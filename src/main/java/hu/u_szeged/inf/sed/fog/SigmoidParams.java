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
        return String.format("SigmoidParams[\n" +
                        "\tpriceLambda=%.05f,\n" +
                        "\tpriceShift=%.05f,\n" +
                        "\tloadOfResourceLambda=%.05f,\n" +
                        "\tloadOfResourceShift=%.05f,\n" +
                        "\tunprocessedDataLambda=%.05f,\n" +
                        "\tunprocessedDataShift=%.05f,\n" +
                        ']',
                priceLambda,
                priceShift,
                loadOfResourceLambda,
                loadOfResourceShift,
                unprocessedDataLambda,
                unprocessedDataShift
        );
    }
}
