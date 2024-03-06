package fi.nls.hakunapi.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import fi.nls.hakunapi.core.join.Join;

public class SimpleJoinGraph {

    private final Map<String, List<Join>> adjacent;

    public SimpleJoinGraph(Set<String> tables, List<Join> joins) {
        adjacent = new HashMap<>();
        for (String table : tables) {
            adjacent.put(table, new ArrayList<>());
        }
        for (Join join : joins) {
            adjacent.get(join.getLeftTable()).add(join);
        }
    }

    private class Edge {
        String table;
        Join join;
        Edge parent;

        public Edge(String table, Join join, Edge parent) {
            this.table = table;
            this.join = join;
            this.parent = parent;
        }
    }

    public List<Join> search(String from, String to) {
        Queue<Edge> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        visited.add(from);
        queue.add(new Edge(from, null, null));
        Edge end = null;
        while (!queue.isEmpty()) {
            Edge edge = queue.poll();
            if (edge.table.equals(to)) {
                end = edge;
                break;
            }
            for (Join j : adjacent.get(edge.table)) {
                String joinedTable = edge.table.equals(j.getLeftTable()) ? j.getRightTable() : j.getLeftTable();
                if (!visited.contains(joinedTable)) {
                    visited.add(joinedTable);
                    queue.add(new Edge(joinedTable, j, edge));
                }
            }
        }
        if (end == null) {
            return null;
        }
        List<Join> joins = new ArrayList<>();
        while (end.parent != null) {
            joins.add(end.join);
            end = end.parent;
        }
        Collections.reverse(joins);
        return joins;
    }

   

}
