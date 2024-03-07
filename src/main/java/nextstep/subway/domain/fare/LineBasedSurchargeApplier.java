package nextstep.subway.domain.fare;

import java.util.List;
import lombok.AllArgsConstructor;
import nextstep.subway.domain.Line;

@AllArgsConstructor
public class LineBasedSurchargeApplier extends FareApplier {

  private final List<Line> lines;

  @Override
  public Fare calculate(Fare fare) {
    lines.stream()
        .mapToInt(Line::getExtraFare)
        .max()
        .ifPresent(fare::addSurcharge);

    return fare;
  }
}
