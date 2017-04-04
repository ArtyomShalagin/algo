package veb;

public class VEBTree implements IntegerSet {
    private long min, max;
    private IntegerSet[] clusters;
//    private Map<Long, IntegerSet> clusters;
    private IntegerSet aux;
//    private IntegerSet aux;
    private int w;
    private int loLen, hiLen;

    public VEBTree(int w) {
        this.w = w;
        min = max = NO;
        loLen = w % 2 == 0 ? w / 2 : w / 2 + 1;
        hiLen = w / 2;
    }

    private long lo(long x) {
        // if w % 2 == 1 then lower part is 1 bit bigger
        return x & ((1L << loLen) - 1);
    }

    private long hi(long x) {
        // if w % 2 == 1 then upper part is 1 bit smaller
        return x >> loLen;
    }

    private long join(long hi, long lo) {
        return (hi << loLen) | lo;
    }

    private IntegerSet getCluster(long index) {
        if (clusters[(int) index] == null) {
            if (w == 2) {
                clusters[(int) index] = new PrimitiveVEB();
            } else {
                clusters[(int) index] = new VEBTree(loLen);
            }
        }
        return clusters[(int) index];
//        if (!clusters.containsKey(index)) {
//            clusters.put(index, w == 2 ? new PrimitiveVEB() : new VEBTree(loLen));
//        }
//        return clusters.get(index);
    }

    private void init() {
        if (clusters == null) {
            if (w == 2) {
                clusters = new PrimitiveVEB[2];
//                clusters = new HashMap<>();
                aux = new PrimitiveVEB();
            } else {
                clusters = new VEBTree[1 << hiLen];
//                clusters = new HashMap<>();
                aux = new VEBTree(loLen);
            }
        }
    }

    @Override
    public void add(long x) {
        if (min == NO) {
            min = max = x;
            return;
        }
        if (x == min) {
            return;
        }
        if (x < min) {
            long t = x;
            x = min;
            min = t;
        }
        if (x > max) {
            max = x;
        }
        init();
        long hi = hi(x), lo = lo(x);
        IntegerSet cluster = getCluster(hi);
        if (cluster.getMin() == NO) {
            aux.add(hi);
        }
        cluster.add(lo);
    }

    @Override
    public void remove(long x) {
        init();
        if (x == min) {
            if (aux.getMin() == NO) {
                min = NO;
                max = NO;
            } else {
                long minHi = aux.getMin();
                IntegerSet cluster = getCluster(minHi);
                min = join(minHi, cluster.getMin());
                cluster.remove(cluster.getMin());
                if (cluster.getMin() == NO) {
                    aux.remove(minHi);
                }
            }
        } else {
            long hi = hi(x), lo = lo(x);
            IntegerSet cluster = getCluster(hi);
            cluster.remove(lo);
            if (cluster.getMin() == NO) {
                aux.remove(hi);
            }
            if (aux.getMin() == NO) {
                max = min;
            } else {
                max = join(aux.getMax(), getCluster(aux.getMax()).getMax());
            }
        }
    }

    @Override
    public long next(long x) {
        if (min == NO) {
            return NO;
        }
        if (x < min) {
            return min;
        }
        init();
        long hi = hi(x), lo = lo(x);
        IntegerSet cluster = getCluster(hi);
        if (cluster.getMin() != NO && lo < cluster.getMax()) {
            return join(hi, cluster.next(lo));
        }
        long nextHi = aux.next(hi);
        if (nextHi == NO) {
            return NO;
        }
        cluster = getCluster(nextHi);
        return join(nextHi, cluster.getMin());
    }

    @Override
    public long prev(long x) {
        if (min == NO || x <= min) {
            return NO;
        }
        if (x > max) {
            return max;
        }
        init();
        long hi = hi(x), lo = lo(x);
        IntegerSet cluster = getCluster(hi);
        if (cluster.getMin() != NO && lo > cluster.getMin()) {
            return join(hi, cluster.prev(lo));
        }
        long prevHi = aux.prev(hi);
        if (prevHi == NO) {
            return min;
        }
        return join(prevHi, getCluster(prevHi).getMax());
    }

    @Override
    public long getMin() {
        return min;
    }

    @Override
    public long getMax() {
        return max;
    }

    private static class PrimitiveVEB implements IntegerSet {
        private boolean[] data = new boolean[2];

        PrimitiveVEB() {
        }

        @Override
        public void add(long x) {
            data[(int) x] = true;
        }

        @Override
        public void remove(long x) {
            data[(int) x] = false;
        }

        @Override
        public long next(long x) {
            if (x == 1) {
                return NO;
            } else {
                return data[1] ? 1 : NO;
            }
        }

        @Override
        public long prev(long x) {
            if (x == 0) {
                return NO;
            } else {
                return data[0] ? 0 : NO;
            }
        }

        @Override
        public long getMin() {
            if (data[0]) {
                return 0;
            } else if (data[1]) {
                return 1;
            }
            return NO;
        }

        @Override
        public long getMax() {
            if (data[1]) {
                return 1;
            } else if (data[0]) {
                return 0;
            }
            return NO;
        }
    }
}
