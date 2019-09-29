public class CategoricalProbabilities {
    private double regularProbability, spamProbability;

    public CategoricalProbabilities(double regularProbability, double spamProbability) {
        this.regularProbability = regularProbability;
        this.spamProbability = spamProbability;
    }

    public double getRegularProbability() {
        return regularProbability;
    }

    public double getSpamProbability() {
        return spamProbability;
    }
}
