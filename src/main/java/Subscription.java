import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Subscription {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private double charge;
    private SubscriptionType subscriptionType;
    private String dayOfSubscription;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> invoiceDate;

    private Subscription(final double charge,
                         final SubscriptionType subscriptionType,
                         final LocalDate startDate,
                         final LocalDate endDate,
                         final String dayOfSubscription) {
        this.charge = charge;
        this.subscriptionType = subscriptionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dayOfSubscription = dayOfSubscription;
        this.invoiceDate = getInvoiceDate(this.subscriptionType, this.startDate, this.endDate);
    }

    private List<String> getInvoiceDate(final SubscriptionType type, final LocalDate startDate, final LocalDate endDate) {
        final List<String> invoiceDate = new ArrayList<>();

        if (type == SubscriptionType.DAILY) {
            return Collections.singletonList(startDate.format(DATE_FORMAT));
        } else if (type == SubscriptionType.WEEKLY) {
            final long numberOfWeeks = ChronoUnit.WEEKS.between(startDate, endDate);
            LocalDate date = startDate;
            for (int i = 0; i < numberOfWeeks + 1; i++) {
                invoiceDate.add(date.format(DATE_FORMAT));
                date = date.plusWeeks(1);
            }
            assert invoiceDate.get((int) numberOfWeeks).equals(endDate.format(DATE_FORMAT));
            return invoiceDate;
        } else if (type == SubscriptionType.MONTHLY) {
            final long numberOfmonths = ChronoUnit.MONTHS.between(startDate, endDate);
            LocalDate date = startDate;
            for (int i = 0; i < numberOfmonths + 1; i++) {
                invoiceDate.add(date.format(DATE_FORMAT));
                date = date.plusMonths(1);
            }
            assert invoiceDate.get((int) numberOfmonths).equals(endDate.format(DATE_FORMAT));
            return invoiceDate;
        }
        return Collections.emptyList();
    }

    private double getCharge() {
        return charge;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    private LocalDate getStartDate() {
        return startDate;
    }

    private LocalDate getEndDate() {
        return endDate;
    }

    public double getChargePerInvoice() {
        return charge * invoiceDate.size();
    }

    public List<String> getInvoiceDate() {
        return invoiceDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Double.compare(that.charge, charge) == 0 &&
                subscriptionType == that.subscriptionType &&
                dayOfSubscription.equals(that.dayOfSubscription) &&
                startDate.equals(that.startDate) &&
                endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charge, subscriptionType, dayOfSubscription, startDate, endDate);
    }

    public static final class Builder {
        private double charge = 0.00;
        private SubscriptionType subscriptionType;
        private LocalDate startDate;
        private LocalDate endDate;

        private Builder() {
        }

        private Builder(Subscription subscription) {
            this.charge = subscription.getCharge();
            this.subscriptionType = subscription.getSubscriptionType();
            this.startDate = subscription.getStartDate();
            this.endDate = subscription.getEndDate();
        }

        public double getCharge() {
            return charge;
        }

        public SubscriptionType getSubscriptionType() {
            return subscriptionType;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public Builder setCharge(final double charge) {
            if (charge < 0) {
                throw new IllegalStateException("Charge rate must be larger than 0");
            }
            this.charge = charge;
            return this;
        }

        public Builder setSubscriptionType(final SubscriptionType subscriptionType) {
            this.subscriptionType = subscriptionType;
            return this;
        }

        public Builder setStartDate(final String startDate) {
            try {
                this.startDate = LocalDate.parse(startDate, DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                throw new IllegalStateException("Please enter a valid start date in format (dd/MM/yyyy).");
            }
            return this;
        }

        public Builder setEndDate(final String endDate) {
            try {
                this.endDate = LocalDate.parse(endDate, DATE_FORMAT);
            } catch (DateTimeParseException ex) {
                throw new IllegalStateException("Please enter a valid end date in format (dd/MM/yyyy).");
            }
            return this;
        }

        public Subscription build() {

            if (subscriptionType == null || startDate == null || endDate == null) {
                throw new IllegalStateException("Please ensure subscription type, start date and end date are being entered.");
            }

            final String dayOfSubscription;

            // Get day of subscription
            if (this.subscriptionType == SubscriptionType.WEEKLY) {
                dayOfSubscription = startDate.getDayOfWeek().toString();
            } else if (this.subscriptionType == SubscriptionType.DAILY || this.subscriptionType == SubscriptionType.MONTHLY) {
                dayOfSubscription = String.valueOf(startDate.getDayOfMonth());
            } else {
                dayOfSubscription = "";
            }

            validateWeeklySubscriptionDate(this.subscriptionType, this.startDate, this.endDate, dayOfSubscription);

            return new Subscription(charge, subscriptionType, startDate, endDate, dayOfSubscription);
        }

        private void validateWeeklySubscriptionDate(final SubscriptionType type,
                                                    final LocalDate startDate,
                                                    final LocalDate endDate,
                                                    final String dayOfSubscription) {
            // Ensure the end date is at the right date
            if (type == SubscriptionType.DAILY && !startDate.isEqual(endDate)) {
                throw new IllegalStateException("For daily subscription, the start date must be same as the end date.");
            } else if (type == SubscriptionType.WEEKLY || type == SubscriptionType.MONTHLY) {
                if (startDate.isAfter(endDate)) {
                    throw new IllegalStateException("For weekly and monthly subscription, the end date must be after the start date.");
                }

                if (type == SubscriptionType.WEEKLY && !Objects.equals(dayOfSubscription, endDate.getDayOfWeek().toString())) {
                    throw new IllegalStateException("For weekly subscription, the day of week of end date must be the same as the start date (" +
                            startDate.toString() + " - " + startDate.getDayOfWeek() + ").");
                } else if (this.subscriptionType == SubscriptionType.MONTHLY && !Objects.equals(dayOfSubscription, String.valueOf(endDate.getDayOfMonth()))) {
                    throw new IllegalStateException("For monthly subscription, the day of month of end date must be the same as the start date (" +
                            startDate.toString() + " - " + startDate.getDayOfMonth() + ").");
                }
            }

            // Ensure subscription is not more that 3 months
            final Period period = Period.between(startDate, endDate);
            if (period.getMonths() > 3 || (period.getMonths() == 3 && period.getDays() > 0)) {
                throw new IllegalStateException("You can only have maximum 3 months subscription.");
            }
        }
    }
}

