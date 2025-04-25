package hu.u_szeged.inf.sed.fog;

public record SigmoidParams(
        double priceLambda,
        Double priceShift,
        double loadOfResourceLambda,
        Double loadOfResourceShift,
        double unprocessedDataLambda,
        Double unprocessedDataShift
) {
    @Override
    public String toString() {
        return String.format("SigmoidParams{" +
                        "priceLambda=%.05f; " +
                        "priceShift=%.05f; " +
                        "loadOfResourceLambda=%.05f; " +
                        "loadOfResourceShift=%.05f; " +
                        "unprocessedDataLambda=%.05f; " +
                        "unprocessedDataShift=%.05f" +
                        '}',
                priceLambda,
                priceShift,
                loadOfResourceLambda,
                loadOfResourceShift,
                unprocessedDataLambda,
                unprocessedDataShift
        );
    }
}
