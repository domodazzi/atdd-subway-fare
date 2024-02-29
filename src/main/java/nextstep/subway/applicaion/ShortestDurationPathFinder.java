package nextstep.subway.applicaion;

import java.util.Collection;
import java.util.Optional;
import nextstep.subway.domain.PathFinder;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.vo.Path;
import nextstep.subway.ui.BusinessException;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;

public class ShortestDurationPathFinder implements PathFinder {

  private final WeightedMultigraph<Station, PathWeightedEdge> graph;

  public ShortestDurationPathFinder(final Collection<Section> sections) {
    graph = buildGraph(sections);
  }

  @Override
  public Optional<Path> find(final Station source, final Station target) {
    verifyRequiredArguments(source, target);

    final var path = new DijkstraShortestPath<>(graph).getPath(source, target);

    if (path == null) {
      throw new BusinessException("경로를 찾을 수 없습니다.");
    }

    final int distance = path.getEdgeList().stream()
        .mapToInt(PathWeightedEdge::getDistance)
        .sum();

    final int duration = path.getEdgeList().stream()
        .mapToInt(PathWeightedEdge::getDuration)
        .sum();

    return Optional.ofNullable(path)
        .map(it -> Path.from(it.getVertexList(), distance, duration));
  }

  @Override
  public boolean isPathExists(Station source, Station target) {
    final var path = new DijkstraShortestPath<>(graph).getPath(source, target);
    return path != null;
  }

  private void verifyRequiredArguments(Station source, Station target) {
    if (source == null) {
      throw new IllegalArgumentException("출발역 정보가 없습니다.");
    }

    if (target == null) {
      throw new IllegalArgumentException("도착역 정보가 없습니다.");
    }
  }

  private static WeightedMultigraph<Station, PathWeightedEdge> buildGraph(final Collection<Section> sections) {
    if (sections == null) {
      throw new IllegalArgumentException("구간 정보가 없습니다.");
    }

    final var graph = WeightedMultigraph.<Station, PathWeightedEdge>builder(PathWeightedEdge.class).build();

    sections.forEach(section -> {
      // add station ID
      graph.addVertex(section.getUpStation());
      graph.addVertex(section.getDownStation());

      // add edge
      final var edge = new PathWeightedEdge(section.getDistance(), section.getDuration());
      graph.addEdge(
          section.getUpStation(),
          section.getDownStation(),
          edge
      );

      graph.setEdgeWeight(edge, edge.getDuration());
    });

    return graph;
  }
}
