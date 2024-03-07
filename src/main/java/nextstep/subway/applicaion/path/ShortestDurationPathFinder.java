package nextstep.subway.applicaion.path;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.PathFinder;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.vo.Path;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;

public class ShortestDurationPathFinder implements PathFinder {

  private final DijkstraShortestPath<Station, PathWeightedEdge> path;

  public ShortestDurationPathFinder(final Collection<Section> sections) {
    path = buildPath(sections);
  }

  private DijkstraShortestPath<Station, PathWeightedEdge> buildPath(final Collection<Section> sections) {
    if (sections == null) {
      throw new IllegalArgumentException("구간 정보가 없습니다.");
    }

    final var graph = WeightedMultigraph.<Station, PathWeightedEdge>builder(PathWeightedEdge.class).build();

    sections.forEach(section -> {
      // add station ID
      graph.addVertex(section.getUpStation());
      graph.addVertex(section.getDownStation());

      // add edge
      final var edge = new PathWeightedEdge(section.getLine(), section.getDistance(), section.getDuration());
      graph.addEdge(
          section.getUpStation(),
          section.getDownStation(),
          edge
      );

      graph.setEdgeWeight(edge, edge.getDuration());
    });

    return new DijkstraShortestPath<>(graph);
  }

  @Override
  public Optional<Path> find(final Station source, final Station target) {
    verifyRequiredArguments(source, target);

    final var path = this.path.getPath(source, target);

    if (path == null) {
      return Optional.empty();
    }

    final int distance = path.getEdgeList().stream()
        .mapToInt(PathWeightedEdge::getDistance)
        .sum();

    final int duration = path.getEdgeList().stream()
        .mapToInt(PathWeightedEdge::getDuration)
        .sum();

    final List<Line> lines = path.getEdgeList().stream()
        .map(PathWeightedEdge::getLine)
        .collect(Collectors.toList());

    return Optional.of(path)
        .map(it -> Path.from(it.getVertexList(), distance, duration, lines));
  }

  @Override
  public boolean isPathExists(Station source, Station target) {
    return path.getPath(source, target) != null;
  }

  private void verifyRequiredArguments(Station source, Station target) {
    if (source == null) {
      throw new IllegalArgumentException("출발역 정보가 없습니다.");
    }

    if (target == null) {
      throw new IllegalArgumentException("도착역 정보가 없습니다.");
    }
  }
}
