package test;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import veb.IntegerSet;
import veb.VEBTree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VEBTest {
    private int w = 25;
    private long maxValue = (1L << w) - 1;
    private Random rnd = new Random(42);

    private List<Long> generate(int n) {
        return Stream
                .generate(() -> Math.abs(rnd.nextLong()) % (maxValue + 1))
                .limit(n)
                .collect(Collectors.toList());
    }

    private boolean contains(IntegerSet set, long val) {
        if (val == 0) {
            return set.prev(val + 1) == val;
        }
        return set.next(val - 1) == val;
    }

    @Test
    public void test1_addRemovePrevNext() {
        System.out.println("testing addRemovePrevNext");
        final int n = 100_000;
        List<Long> data = generate(n);
        IntegerSet veb = new VEBTree(w);
        data.forEach(veb::add);
        for (long val : data) {
            if (val != 0) {
                assertEquals(veb.next(val - 1) == val, true);
            }
            if (val != maxValue) {
                assertEquals(veb.prev(val + 1) == val, true);
            }
        }
        System.out.println("\tcontains done");
        data.forEach(veb::remove);
        for (long val : data) {
            assertEquals(contains(veb, val), false);
        }
        System.out.println("\tremove done");
    }

    @Test
    public void test2_randomContains() {
        System.out.println("testing randomContains");
        final int n = 100_000;
        IntegerSet veb = new VEBTree(w);
        List<Long> data =
                generate(n / 2).stream()
                        .map(val -> val % n)
                        .collect(Collectors.toList());
        data.forEach(veb::add);
        Set<Long> set = new HashSet<>(data);
        for (int i = 0; i < n; i++) {
            long val = Math.abs(rnd.nextLong()) % n;
            assertEquals(contains(veb, val), set.contains(val));
        }
    }

    @Test
    public void test3_walk() {
        System.out.println("testing walk");
        final int n = 100_000;
        List<Long> data =
                generate(n).stream()
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList());
        IntegerSet veb = new VEBTree(w);
        data.forEach(veb::add);
        long curr = veb.getMin();
        for (long val : data) {
            assertEquals(curr, val);
            curr = veb.next(curr);
        }
        System.out.println("\tstart to end done");
        assertEquals(curr, -1);
        curr = veb.getMax();
        data = data.stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        for (long val : data) {
            assertEquals(curr, val);
            curr = veb.prev(curr);
        }
        System.out.println("\tend to start done");
    }

    private long speedAdd(IntegerSet set, List<Long> data) {
        long start = System.currentTimeMillis();
        data.forEach(set::add);
        return System.currentTimeMillis() - start;
    }

    private long speedWalk(IntegerSet set) {
        long start = System.currentTimeMillis();
        long curr = set.getMin();
        while (curr != IntegerSet.NO) {
            curr = set.next(curr);
        }
        curr = set.getMax();
        while (curr != IntegerSet.NO) {
            curr = set.prev(curr);
        }
        return System.currentTimeMillis() - start;
    }

    private long speedRemove(IntegerSet set, List<Long> data) {
        long start = System.currentTimeMillis();
        data.forEach(set::remove);
        return System.currentTimeMillis() - start;
    }

    @Test
    public void test4_performance() {
        System.out.println("testing performance, note: no assertions here, you decide if it is passed or not :)");
        int n = 5_000_000;
        List<Long> data = generate(n);
        System.gc();
        {
            IntegerSet veb = new VEBTree(w);
            long add = speedAdd(veb, data);
            System.out.format("\tVEB add: %d ms\n", add);
            long walk = speedWalk(veb);
            System.out.format("\tVEB walk: %d ms\n", walk);
            long remove = speedRemove(veb, data);
            System.out.format("\tVEB remove: %d ms\n", remove);
        }
        System.gc();
        {
            IntegerSet set = new IntegerTreeSet();
            long add = speedAdd(set, data);
            System.out.format("\tTreeSet add: %d ms\n", add);
            long walk = speedWalk(set);
            System.out.format("\tTreeSet walk: %d ms\n", walk);
            long remove = speedRemove(set, data);
            System.out.format("\tTreeSet remove: %d ms\n", remove);
        }

    }

    static class IntegerTreeSet implements IntegerSet {

        private TreeSet<Long> set = new TreeSet<>();

        private long check(Long val) {
            return val == null ? NO : val;
        }

        @Override
        public void add(long x) {
            set.add(x);
        }

        @Override
        public void remove(long x) {
            set.remove(x);
        }

        @Override
        public long next(long x) {
            return check(set.higher(x));
        }

        @Override
        public long prev(long x) {
            return check(set.lower(x));
        }

        @Override
        public long getMin() {
            return check(set.first());
        }

        @Override
        public long getMax() {
            return check(set.last());
        }
    }
}
