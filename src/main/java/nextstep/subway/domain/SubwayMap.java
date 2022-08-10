package nextstep.subway.domain;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.util.List;
import java.util.stream.Collectors;

public class SubwayMap {
    private List<Line> lines;

    public SubwayMap(List<Line> lines) {
        this.lines = lines;
    }

    public Path findPath(Station source, Station target, ShortestPathType type) {
        SimpleDirectedWeightedGraph<Station, SectionEdge> graph = new SimpleDirectedWeightedGraph<>(SectionEdge.class);

        // 지하철 역(정점)을 등록
        addStationAsVertex(graph);

        // 지하철 역의 연결 정보(간선)을 등록
        addEdge(graph, type);

        // 다익스트라 최단 경로 찾기
        List<Section> sections = findShortestPathSections(source, target, graph);
        return new Path(new Sections(sections));
    }

    private void addStationAsVertex(SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        lines.stream()
                .flatMap(it -> it.getStations().stream())
                .distinct()
                .collect(Collectors.toList())
                .forEach(graph::addVertex);
    }

    private void addEdge(SimpleDirectedWeightedGraph<Station, SectionEdge> graph, ShortestPathType type) {
        lines.stream()
                .flatMap(it -> it.getSections().stream())
                .forEach(it -> setEdge(graph, type, it));

        lines.stream()
                .flatMap(it -> it.getSections().stream())
                .map(it -> new Section(it.getLine(), it.getDownStation(), it.getUpStation(), it.getDistance(), it.getDuration()))
                .forEach(it -> setEdge(graph, type, it));
    }

    private void setEdge(SimpleDirectedWeightedGraph<Station, SectionEdge> graph, ShortestPathType type, Section section) {
        SectionEdge sectionEdge = SectionEdge.of(section);
        graph.addEdge(section.getUpStation(), section.getDownStation(), sectionEdge);
        setEdgeWeight(graph, type, section, sectionEdge);
    }

    private void setEdgeWeight(SimpleDirectedWeightedGraph<Station, SectionEdge> graph,
                               ShortestPathType type, Section section, SectionEdge sectionEdge) {
        if (type == ShortestPathType.DISTANCE) {
            graph.setEdgeWeight(sectionEdge, section.getDistance());
            return;
        }

        if (type == ShortestPathType.DURATION) {
            graph.setEdgeWeight(sectionEdge, section.getDuration());
            return;
        }

        throw new IllegalArgumentException("경로 조회의 타입이 잘못되었습니다.");
    }

    private List<Section> findShortestPathSections(Station source, Station target, SimpleDirectedWeightedGraph<Station, SectionEdge> graph) {
        DijkstraShortestPath<Station, SectionEdge> dijkstraShortestPath = new DijkstraShortestPath<>(graph);
        GraphPath<Station, SectionEdge> result = dijkstraShortestPath.getPath(source, target);

        return result.getEdgeList().stream()
                .map(SectionEdge::getSection)
                .collect(Collectors.toList());
    }
}
