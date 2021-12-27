package com.loganchang.MyRuns5;

/**
 * Code generated with Weka 3.8.5
 * <p>
 * This code is public domain and comes with no warranty.
 * <p>
 * Timestamp: Mon Feb 15 20:45:34 EST 2021
 */

public class WekaClassifier {

    public static double classify(Object[] i)
            throws Exception {

        return WekaClassifier.N22fc6a520(i);
    }

    static double N22fc6a520(Object[] i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 0;
        } else if (((Double) i[0]) <= 104.628754) {
            p = WekaClassifier.N683fc0fd1(i);
        } else if (((Double) i[0]) > 104.628754) {
            p = WekaClassifier.N788bfce65(i);
        }
        return p;
    }

    static double N683fc0fd1(Object[] i) {
        double p = Double.NaN;
        if (i[18] == null) {
            p = 0;
        } else if (((Double) i[18]) <= 0.79987) {
            p = 0;
        } else if (((Double) i[18]) > 0.79987) {
            p = WekaClassifier.N36dca8182(i);
        }
        return p;
    }

    static double N36dca8182(Object[] i) {
        double p = Double.NaN;
        if (i[18] == null) {
            p = 0;
        } else if (((Double) i[18]) <= 1.0394) {
            p = 0;
        } else if (((Double) i[18]) > 1.0394) {
            p = WekaClassifier.N2f4ab0a63(i);
        }
        return p;
    }

    static double N2f4ab0a63(Object[] i) {
        double p = Double.NaN;
        if (i[13] == null) {
            p = 2;
        } else if ((Double) i[13] <= 1.081497) {
            p = 2;
        } else if ((Double) i[13] > 1.081497) {
            p = WekaClassifier.N6ea5e1e74(i);
        }
        return p;
    }

    static double N6ea5e1e74(Object[] i) {
        double p = Double.NaN;
        if (i[5] == null) {
            p = 1;
        } else if ((Double) i[5] <= 5.227016) {
            p = 1;
        } else if ((Double) i[5] > 5.227016) {
            p = 0;
        }
        return p;
    }

    static double N788bfce65(Object[] i) {
        double p = Double.NaN;
        if (i[64] == null) {
            p = 1;
        } else if ((Double) i[64] <= 16.381435) {
            p = WekaClassifier.N2bc407876(i);
        } else if ((Double) i[64] > 16.381435) {
            p = 2;
        }
        return p;
    }

    static double N2bc407876(Object[] i) {
        double p = Double.NaN;
        if (i[0] == null) {
            p = 1;
        } else if (((Double) i[0]) <= 156.587825) {
            p = WekaClassifier.N51715dd97(i);
        } else if (((Double) i[0]) > 156.587825) {
            p = WekaClassifier.N37d186f09(i);
        }
        return p;
    }

    static double N51715dd97(Object[] i) {
        double p = Double.NaN;
        if (i[26] == null) {
            p = 1;
        } else if (((Double) i[26]) <= 2.290335) {
            p = WekaClassifier.N109a34c98(i);
        } else if (((Double) i[26]) > 2.290335) {
            p = 0;
        }
        return p;
    }

    static double N109a34c98(Object[] i) {
        double p = Double.NaN;
        if (i[5] == null) {
            p = 1;
        } else if ((Double) i[5] <= 7.647615) {
            p = 1;
        } else if ((Double) i[5] > 7.647615) {
            p = 2;
        }
        return p;
    }

    static double N37d186f09(Object[] i) {
        double p = Double.NaN;
        if (i[9] == null) {
            p = 1;
        } else if (((Double) i[9]) <= 10.427541) {
            p = 1;
        } else if (((Double) i[9]) > 10.427541) {
            p = WekaClassifier.N3ca257b210(i);
        }
        return p;
    }

    static double N3ca257b210(Object[] i) {
        double p = Double.NaN;
        if (i[20] == null) {
            p = 2;
        } else if ((Double) i[20] <= 4.564063) {
            p = 2;
        } else if ((Double) i[20] > 4.564063) {
            p = WekaClassifier.N67665a6a11(i);
        }
        return p;
    }

    static double N67665a6a11(Object[] i) {
        double p = Double.NaN;
        if (i[26] == null) {
            p = 1;
        } else if ((Double) i[26] <= 4.646162) {
            p = WekaClassifier.N261b163312(i);
        } else if ((Double) i[26] > 4.646162) {
            p = 1;
        }
        return p;
    }

    static double N261b163312(Object[] i) {
        double p = Double.NaN;
        if (i[8] == null) {
            p = 1;
        } else if ((Double) i[8] <= 10.715146) {
            p = 1;
        } else if ((Double) i[8] > 10.715146) {
            p = 2;
        }
        return p;
    }
}
