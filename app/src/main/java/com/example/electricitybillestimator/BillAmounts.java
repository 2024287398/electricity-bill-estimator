package com.example.electricitybillestimator;

final class BillAmounts {

    final double totalCharges;
    final double finalCost;

    private BillAmounts(double totalCharges, double finalCost) {
        this.totalCharges = totalCharges;
        this.finalCost = finalCost;
    }

    static BillAmounts calculate(int units, int rebate) {
        int firstBlock = Math.min(units, 200);
        int secondBlock = Math.min(Math.max(units - 200, 0), 100);
        int thirdBlock = Math.min(Math.max(units - 300, 0), 300);
        int fourthBlock = Math.max(units - 600, 0);

        // Rates are in thousandths of a ringgit. For example, 218 means RM 0.218.
        int chargeInThousandths = firstBlock * 218
                + secondBlock * 334
                + thirdBlock * 516
                + fourthBlock * 546;

        double totalCharges = chargeInThousandths / 1000.0;
        double finalCost = chargeInThousandths * (100 - rebate) / 100000.0;
        return new BillAmounts(totalCharges, finalCost);
    }
}