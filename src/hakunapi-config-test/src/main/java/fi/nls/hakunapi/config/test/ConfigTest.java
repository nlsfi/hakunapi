package fi.nls.hakunapi.config.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fi.nls.hakunapi.core.SimpleSource;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.simple.postgis.PostGISSimpleSource;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

public class ConfigTest {

    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
        }
        boolean verbose = "-v".equals(args[0]);
        String config = args[args.length - 1];

        Map<String, SimpleSource> sourcesByType = new HashMap<>();
        SimpleSource pg = new PostGISSimpleSource();
        sourcesByType.put(pg.getType(), pg);
        
        Path path = Paths.get(config).toAbsolutePath();

        if (verbose) {
            System.out.println("Properties file to use: " + path.toString());
        }

        Properties properties = new Properties();
        try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(r);
        } catch (IOException e) {
            System.err.println("Failed to read properties file!");
            e.printStackTrace();
            return;
        }
        Path parent = path.getParent();
        if (verbose) {
            System.out.println("Parent path: " + parent.toAbsolutePath().toString());
        }

        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);

        HakunaConfigParser parser = new HakunaConfigParser(properties);

        if (verbose) {
            System.out.println("Handling info");
        }
        Info info = parser.readInfo();
        if (verbose) {
            print(om, info);
        }

        if (verbose) {
            System.out.println("Handling servers");
        }
        List<Server> servers = parser.readServers();
        if (verbose) {
            print(om, servers);
        }

        if (verbose) {
            System.out.println("Handling securitySchemes");
        }
        Optional<Map<String, SecurityScheme>> optionalSecuritySchemes = parser.readSecuritySchemes();
        if (!optionalSecuritySchemes.isPresent()) {
            if (verbose) {
                System.out.println("No securitySchemes present, skipping over securityRequirements");
            }
        } else {
            Map<String, SecurityScheme> securitySchemes = optionalSecuritySchemes.get();
            if (verbose) {
                print(om, securitySchemes);
            }
            if (verbose) {
                System.out.println("Handling securityRequirements");
            }
            List<SecurityRequirement> securityRequirements = securitySchemes.keySet().stream()
                    .map(name -> new SecurityRequirement().addList(name))
                    .collect(Collectors.toList());
            if (verbose) {
                print(om, securityRequirements);
            }
        }

        String[] collectionIds = parser.readCollectionIds();
        if (verbose) {
            System.out.println("collectionIds :" + Arrays.toString(collectionIds));
        }
        for (String id : collectionIds) {
            try {
                if (verbose) {
                    System.out.println("Handling collection " + id);
                }
                parser.readCollection(path, sourcesByType, id);
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
            }
        }

        if (verbose) {
            System.out.println("Finished succesfully!");
        } else {
            System.out.println("OK");
        }
    }

    private static void usage() {
        System.err.println("Usage: java -jar ConfigTest [-v] /path/properties.file");
        System.exit(0);
    }

    private static void print(ObjectMapper om, Object value) {
        try {
            System.out.write(om.writeValueAsBytes(value));
            System.out.println();
        } catch (Exception e) {
            System.err.println("JSON serialization failed: " + e.getMessage());
        }
    }

}
