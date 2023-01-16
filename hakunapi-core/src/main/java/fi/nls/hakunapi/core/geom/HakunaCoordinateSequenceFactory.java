package fi.nls.hakunapi.core.geom;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFactory;
import org.locationtech.jts.geom.Coordinates;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

/**
 * WKBReader calls public CoordinateSequence create(int size, int dimension);
 * for which PackedCoordinateSequenceFactory calls
 * `return new PackedCoordinateSequence.Double(size, dimension, 0);`
 * which is wrong for dimension = 4, measures = 1
 */
public class HakunaCoordinateSequenceFactory implements CoordinateSequenceFactory {

    @Override
    public CoordinateSequence create(Coordinate[] coordinates) {
        int dimension = 3;
        int measures = 0;
        if (coordinates != null && coordinates.length > 1 && coordinates[0] != null) {
            Coordinate first = coordinates[0];
            dimension = Coordinates.dimension(first);
            measures = Coordinates.measures(first);
        }
        return new PackedCoordinateSequence.Double(coordinates, dimension, measures);
    }

    @Override
    public CoordinateSequence create(CoordinateSequence coordSeq) {
        int dimension = coordSeq.getDimension();
        int measures = coordSeq.getMeasures();
        return new PackedCoordinateSequence.Double(coordSeq.toCoordinateArray(), dimension, measures);
    }

    @Override
    public CoordinateSequence create(int size, int dimension) {
        int measures = dimension > 3 ? dimension - 3 : 0; 
        return new PackedCoordinateSequence.Double(size, dimension, measures);
    }

    @Override
    public CoordinateSequence create(int size, int dimension, int measures) {
        return new PackedCoordinateSequence.Double(size, dimension, measures);
    }

}
