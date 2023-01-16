package fi.nls.hakunapi.mvt.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.GeometryTransformer;

public class VisvalingamWhyatt extends GeometryTransformer {

    private final double eps2;

    public VisvalingamWhyatt(double eps) {
        this.eps2 = (eps * eps) * 2;
    }

    private VertexUpdateHeap initHeap(CoordinateSequence coords) {
        VertexUpdateHeap vertices = new VertexUpdateHeap();
        double px = coords.getX(0);
        double py = coords.getY(0);
        Vertex v = new Vertex(0);

        for (int i = 1; i < coords.size(); i++) {
            double x = coords.getX(i);
            double y = coords.getY(i);
            if (x == px && y == py) {
                continue;
            }
            px = x;
            py = y;
            Vertex next = new Vertex(i);
            v.next = next;
            v.updateArea(coords);
            vertices.add(v);
            next.prev = v;
            v = next;
        }
        v.updateArea(coords);
        vertices.add(v);

        return vertices;
    }

    @Override
    protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
        VertexUpdateHeap heap = initHeap(coords);
        while (heap.size > 2) {
            Vertex v = heap.peek();
            if (v.area > eps2) {
                break;
            }
            heap.poll();

            Vertex p = v.prev;
            Vertex n = v.next;

            p.next = n;
            p.updateArea(coords);
            if (p.area != Double.MAX_VALUE) {
                heap.update(p);
            }

            n.prev = p;
            n.updateArea(coords);
            if (n.area != Double.MAX_VALUE) {
                heap.update(n);
            }
        }

        if (heap.size == coords.size()) {
            return coords;
        }

        int dim = coords.getDimension();
        CoordinateSequence csq = factory.getCoordinateSequenceFactory().create(heap.size, dim, coords.getMeasures());
        Arrays.sort(heap.heap, 0, heap.size, Comparator.comparingInt(Vertex::getI));
        for (int i = 0; i < heap.size; i++) {
            int index = heap.heap[i].i;
            for (int d = 0; d < dim; d++) {
                csq.setOrdinate(i, d, coords.getOrdinate(index, d));
            }
        }
        return csq;
    }

    @Override
    protected Geometry transformLineString(LineString geom, Geometry parent) {
        CoordinateSequence original = geom.getCoordinateSequence();
        CoordinateSequence transformed = transformCoordinates(geom.getCoordinateSequence(), geom);
        if (original == transformed) {
            return geom;
        }
        if (transformed == null || transformed.size() < 2) {
            return null;
        }
        return factory.createLineString(transformed);
    }

    @Override
    protected Geometry transformLinearRing(LinearRing geom, Geometry parent) {
        CoordinateSequence original = geom.getCoordinateSequence();
        CoordinateSequence seq = transformCoordinates(geom.getCoordinateSequence(), geom);
        if (original == seq) {
            return geom;
        }
        if (seq == null) {
            return null;
        }
        int seqSize = seq.size();
        // ensure a valid LinearRing
        if (seqSize > 0 && seqSize < 4) {
            return null;
        }
        return factory.createLinearRing(seq);
    }

    @Override
    protected Geometry transformPolygon(Polygon geom, Geometry parent) {
        Geometry shell = transformLinearRing((LinearRing) geom.getExteriorRing(), geom);
        if (shell == null || shell.isEmpty() || !(shell instanceof LinearRing)) {
            return null;
        }

        List<LinearRing> holes = new ArrayList<>();
        for (int i = 0; i < geom.getNumInteriorRing(); i++) {
            Geometry hole = transformLinearRing((LinearRing) geom.getInteriorRingN(i), geom);
            if (hole != null && !hole.isEmpty() && hole instanceof LinearRing) {
                holes.add((LinearRing) hole);
            }
        }

        Geometry g = factory.createPolygon((LinearRing) shell, holes.toArray(new LinearRing[holes.size()]));
        return g;
    }

    static class Vertex implements Comparable<Vertex> {

        protected final int i;
        protected Vertex prev;
        protected Vertex next;
        protected double area;
        protected int heapIndex;

        public Vertex(int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }

        public void updateArea(CoordinateSequence cs) {
            area = prev == null || next == null
                    ? Double.MAX_VALUE
                            : triangleAreaDoubled(cs.getX(prev.i), cs.getY(prev.i), cs.getX(i), cs.getY(i), cs.getX(next.i), cs.getY(next.i)); 
        }

        private double triangleAreaDoubled(double x0, double y0, double x1, double y1, double x2, double y2) {
            return Math.abs((x2 - x0) * (y1 - y0) - (x1 - x0) * (y2 - y0));
            // return Math.abs(((x2 - x0) * (y1 - y0) - (x1 - x0) * (y2 - y0)) / 2);
        }

        @Override
        public int compareTo(Vertex o) {
            return Double.compare(area, o.area);
        }

    }

    static class VertexUpdateHeap {

        protected Vertex[] heap;
        protected int size;

        public VertexUpdateHeap() {
            this.heap = new Vertex[16];
            this.size = 0;
        }

        public VertexUpdateHeap(int initialSize) {
            this.heap = new Vertex[initialSize];
            this.size = 0;
        }

        public void add(Vertex v) {
            int i = size++;
            if (i >= heap.length) {
                heap = Arrays.copyOf(heap, heap.length * 2);
            }
            v.heapIndex = i;
            if (i == 0) {
                heap[i] = v;
            } else {
                siftUp(v);
            }
        }

        public void update(Vertex value) {
            siftUp(value);
            siftDown(value);
        }

        public Vertex peek() {
            return heap[0];
        }

        public Vertex poll() {
            Vertex ret = heap[0];
            Vertex v = heap[--size];
            heap[size] = null;
            v.heapIndex = 0;
            siftDown(v);
            return ret;
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }

        private void siftUp(Vertex v) {
            int i = v.heapIndex;
            while (i > 0) {
                int parent = (i - 1) >>> 1;
        Vertex p = heap[parent];
        if (v.compareTo(p) >= 0) {
            break;
        }
        p.heapIndex = i;
        heap[i] = p;
        i = parent;
            }
            v.heapIndex = i;
            heap[i] = v;
        }

        private void siftDown(Vertex v) {
            int i = v.heapIndex;
            int half = size >>> 1;
            while (i < half) {
                int child = (i << 1) + 1;
                Vertex c = heap[child];
                int right = child + 1;
                if (right < size && c.compareTo(heap[right]) > 0) {
                    c = heap[child = right];
                }
                if (v.compareTo(c) <= 0) {
                    break;
                }
                c.heapIndex = i;
                heap[i] = c;
                i = child;
            }
            v.heapIndex = i;
            heap[i] = v;
        }

    }

}
