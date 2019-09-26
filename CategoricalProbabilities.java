public class CategoricalProbabilities {
    private double regularProbability, spamProbability;

    public CategoricalProbabilities(double regularProbability, double spamProbability) {
        regularProbability = this.regularProbability;
        spamProbability = this.spamProbability;
    }

    public double getRegularProbability() {
        return regularProbability;
    }

    public double getSpamProbability() {
        return spamProbability;
    }
}
