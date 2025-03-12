package hu.u_szeged.inf.sed.fog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        GeneticPliantOptimiser.evolution();
    }
}