import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

public final class Distributions {

    private Distributions() {}

    public static int newPoissonDistributedRandomValue (double mean) {

        return new PoissonDistribution(mean).sample();

    }

    public static int newNormalDistributedValue (double mean, double deviation) {

        return (int) new NormalDistribution(mean, deviation).sample();

    }

    public static int newUniformDistributedValue (int upper, int lower) {

        return new UniformIntegerDistribution(upper, lower).sample();
    }

}
