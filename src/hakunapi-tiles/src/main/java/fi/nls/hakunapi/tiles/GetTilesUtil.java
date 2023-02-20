package fi.nls.hakunapi.tiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MultivaluedMap;

import org.locationtech.jts.geom.Envelope;

import fi.nls.hakunapi.core.FeatureServiceConfig;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.request.GetTileFeaturesRequest;
import fi.nls.hakunapi.core.tiles.TileMatrix;
import fi.nls.hakunapi.core.tiles.TilingScheme;

public class GetTilesUtil {

	static TilingScheme getTilingScheme(FeatureServiceConfig service, String tilingSchemeId) {
		TilingScheme tilingScheme = service.getTilingScheme(tilingSchemeId);
		if (tilingScheme == null) {
			throw new NotFoundException("Unknown tilingSchemeId");
		}
		return tilingScheme;
	}

	static TileMatrix getTileMatrix(TilingScheme tilingScheme, String level) {
		return Arrays.stream(tilingScheme.getTileMatrices()).filter(tm -> tm.getIdentifier().equals(level)).findAny()
				.orElseThrow(() -> new NotFoundException("Unknown level"));
	}

	public static Envelope getTileBounds(TileMatrix tm, String _row, String _col) {
		int row = Integer.parseInt(_row);
		int col = Integer.parseInt(_col);
		if (row < 0 || row >= tm.getMatrixHeight()) {
			throw new IllegalArgumentException("row out of bounds");
		}
		if (col < 0 || col >= tm.getMatrixWidth()) {
			throw new IllegalArgumentException("col out of bounds");
		}
		double pixelSpan = tm.getScaleDenominator() * 0.00028;
		double tileSpanX = tm.getTileWidth() * pixelSpan;
		double tileSpanY = tm.getTileHeight() * pixelSpan;

		double x1 = col * tileSpanX + tm.getTopLeftCorner()[0];
		double y2 = tm.getTopLeftCorner()[1] - row * tileSpanY;
		double x2 = x1 + tileSpanX;
		double y1 = y2 - tileSpanY;

		return new Envelope(x1, x2, y1, y2);
	}

	public static void modifyWithCollectionSpecificParams(FeatureServiceConfig service, GetTileFeaturesRequest request,
			MultivaluedMap<String, String> queryParams) {

		for (final Entry<String, List<String>> param : queryParams.entrySet()) {

			final String key = param.getKey();

			if (!key.contains("[") || !key.contains("]")) {
				continue;
			}
			if (key.indexOf("[") > key.indexOf("[")) {
				continue;
			}
			String[] parts = key.split("[\\W]");
			if (parts.length != 2) {
				continue;
			}

			final String paramName = parts[0];
			final String collectionName = parts[1];
			Optional<String> filterValue = param.getValue().stream().findFirst();
			if (!filterValue.isPresent()) {
				continue;
			}

			request.getCollections().stream().filter(c -> c.getFt().getName().equals(collectionName)).forEach(fc -> {
				final Optional<GetFeatureParam> gfp = fc.getFt().getParameters().stream()
						.filter(p -> p.getParamName().equals(paramName)).findFirst();
				gfp.ifPresent(p -> {
					p.modify(service, request, filterValue.get());
				});
			});

		}

	}

}
