package hu.u_szeged.inf.sed.fog;

public record SigmoidParams(
        double priceLambda,
        Double priceShift,
        double loadOfResourceLambda,
        Double loadOfResourceShift,
        double unprocessedDataLambda,
        Double unprocessedDataShift
) {
}
