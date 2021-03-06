package de.qaware.qav.graph.alg.impl;

import de.qaware.qav.graph.alg.api.CycleFinder;
import de.qaware.qav.graph.api.Dependency;
import de.qaware.qav.graph.api.DependencyGraph;
import de.qaware.qav.graph.api.Node;
import de.qaware.qav.graph.impl.DependencyGraphSimpleImpl;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;

import java.util.ArrayList;
import java.util.List;

/**
 * Detect cycles. This implementation uses the JGraphT algorithm, and therefore only works on the {@link
 * DependencyGraphSimpleImpl}.
 *
 * @author QAware GmbH
 */
@Slf4j
public class CycleFinderImpl implements CycleFinder {

    /**
     * This counter is for all instances, so that multiple cycle detection runs produce unique IDs.
     */
    private static int cycleCounter = 0;

    /**
     * The {@link DependencyGraph} to analyze. It must be a {@link DependencyGraphSimpleImpl} so that we can apply
     * JGraphT's graph algorithms.
     */
    private final DependencyGraphSimpleImpl dependencyGraph;

    private List<List<Node>> cycles;
    private Long duration;

    /**
     * Constructor. Kicks off the cycle detection.
     *
     * @param dependencyGraph the {@link DependencyGraph}
     */
    public CycleFinderImpl(DependencyGraph dependencyGraph) {
        if (dependencyGraph instanceof DependencyGraphSimpleImpl) {
            this.dependencyGraph = (DependencyGraphSimpleImpl) dependencyGraph;
        } else {
            throw new UnsupportedOperationException("Can only work on " + DependencyGraphSimpleImpl.class.getName() + " graphs.");
        }

        detectCycles();
    }

    /**
     * Use a standard algorithm to find cycles.
     *
     * There is no cut-off, i.e. for very large graphs with many cycles, this analysis may take a really long time to
     * finish.
     */
    private void detectCycles() {
        LOGGER.info("Cycle detector: Graph with {} nodes and {} edges", dependencyGraph.getAllNodes().size(), dependencyGraph.getAllEdges().size());

        Long start = System.currentTimeMillis();

        TarjanSimpleCycles<Node, Dependency> detector = new TarjanSimpleCycles<>(this.dependencyGraph.getGraph());
        cycles = detector.findSimpleCycles();

        this.duration = System.currentTimeMillis() - start;

        logResult();
        markNodesInCycles();
    }

    private void logResult() {
        if (hasCycles()) {
            int totalNodes = cycles.stream().mapToInt(List::size).sum();
            LOGGER.error("Cycle detector: Detected {} cycle(s) with {} nodes  ({}ms)", cycles.size(), totalNodes, duration);
            cycles.forEach(this::logBaseRelations);
        } else {
            LOGGER.info("Cycle detector: Detected no cycles ({}ms)", duration);
        }
    }

    /**
     * For each cycle, log all base relations
     */
    @SuppressWarnings("squid:S1698") // compare with "==" instead of .equals()
    private void logBaseRelations(List<Node> nodes) {
        LOGGER.error("Cycle: {}", nodes);
        for (Node source : nodes) {
            for (Node target : nodes) {
                if (source != target) { // yes, compare instances
                    logBaseRelation(source, target);
                }
            }
        }

    }

    private void logBaseRelation(Node source, Node target) {
        Dependency edge = dependencyGraph.getEdge(source, target);
        if (edge != null) {
            LOGGER.info("  {} -> {} [{}]", source.getName(), target.getName(), edge.getBaseDependencies().size());
            edge.getBaseDependencies().forEach(it ->
                    LOGGER.info("    * {} -> {} [{}]", it.getSource().getName(), it.getTarget().getName(), it.getDependencyType())
            );
        }
    }

    /**
     * marks nodes with cycle labels: IN_CYCLE = true for each node CYCLE_LABEL = unique label per cycle
     */
    private void markNodesInCycles() {
        for (List<Node> cycle : cycles) {
            String cycleLabel = "Cycle_" + getCycleNumber();
            cycle.forEach(node -> {
                node.setProperty(IN_CYCLE, true);
                node.setProperty(CYCLE_LABEL, cycleLabel);
            });
        }
    }

    /**
     * unique numbers of cycles
     *
     * @return the next cycle number
     */
    private static synchronized int getCycleNumber() {
        return cycleCounter++;
    }

    @Override
    public final boolean hasCycles() {
        return !cycles.isEmpty();
    }

    @Override
    public List<List<Node>> getCycles() {
        return new ArrayList<>(cycles);
    }
}
