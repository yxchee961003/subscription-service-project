import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SubscriptionTest {

    @Test
    void testDailySubscription() {
        final double charge = 10.00;
        final String startDate = formatDate(LocalDate.now());

        final Subscription dailySubscription = Subscription.newBuilder()
                .setCharge(charge)
                .setSubscriptionType(SubscriptionType.DAILY)
                .setStartDate(startDate)
                .setEndDate(startDate)
                .build();

        assertThat(dailySubscription.getChargePerInvoice()).isEqualTo(charge);
        assertThat(dailySubscription.getSubscriptionType()).isEqualTo(SubscriptionType.DAILY);
        assertThat(dailySubscription.getInvoiceDate()).containsExactlyElementsOf(Collections.singletonList(startDate));
    }

    @Test
    void testWeeklySubscription() {
        final double charge = 10.00;
        final long numberOfWeek = 3;
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.plusWeeks(numberOfWeek);
        final String formattedStartDate = formatDate(startDate);
        final String formattedEndDate = formatDate(endDate);

        final Subscription weeklySubscription = Subscription.newBuilder()
                .setCharge(charge)
                .setSubscriptionType(SubscriptionType.WEEKLY)
                .setStartDate(formattedStartDate)
                .setEndDate(formattedEndDate)
                .build();

        List<String> expectedInvoiceDate = Arrays.asList(
                formattedStartDate,
                formatDate(startDate.plusWeeks(1)),
                formatDate(startDate.plusWeeks(2)),
                formattedEndDate
        );

        assertThat(weeklySubscription.getChargePerInvoice()).isEqualTo(charge * (numberOfWeek + 1));
        assertThat(weeklySubscription.getSubscriptionType()).isEqualTo(SubscriptionType.WEEKLY);
        assertThat(weeklySubscription.getInvoiceDate()).containsExactlyElementsOf(expectedInvoiceDate);
    }

    @Test
    void testMonthlySubscription() {
        final double charge = 10.00;
        final long numberOfMonths = 3;
        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.plusMonths(numberOfMonths);
        final String formattedStartDate = formatDate(startDate);
        final String formattedEndDate = formatDate(endDate);

        final Subscription monthlySubscription = Subscription.newBuilder()
                .setCharge(charge)
                .setSubscriptionType(SubscriptionType.MONTHLY)
                .setStartDate(formattedStartDate)
                .setEndDate(formattedEndDate)
                .build();

        List<String> expectedInvoiceDate = Arrays.asList(
                formattedStartDate,
                formatDate(startDate.plusMonths(1)),
                formatDate(startDate.plusMonths(2)),
                formattedEndDate
        );

        assertThat(monthlySubscription.getChargePerInvoice()).isEqualTo(charge * (numberOfMonths + 1));
        assertThat(monthlySubscription.getSubscriptionType()).isEqualTo(SubscriptionType.MONTHLY);
        assertThat(monthlySubscription.getInvoiceDate()).containsExactlyElementsOf(expectedInvoiceDate);
    }

    @Test
    void testInvalidDate() {
        // Todo: if gt time, make the error messages reusable

        final String invalidDateFormat = LocalDate.now().toString();

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setCharge(-5.0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Charge rate must be larger than 0");

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setCharge(10.00)
                        .setStartDate(invalidDateFormat))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Please enter a valid start date in format (dd/MM/yyyy).");

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setCharge(10.00)
                        .setEndDate(invalidDateFormat))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Please enter a valid end date in format (dd/MM/yyyy).");

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Please ensure subscription type, start date and end date are being entered.");

        final LocalDate startDate = LocalDate.now();
        final LocalDate endDate = startDate.plusMonths(4);

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setSubscriptionType(SubscriptionType.MONTHLY)
                        .setStartDate(formatDate(startDate))
                        .setEndDate(formatDate(endDate))
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("You can only have maximum 3 months subscription.");

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setSubscriptionType(SubscriptionType.DAILY)
                        .setStartDate(formatDate(startDate))
                        .setEndDate(formatDate(endDate))
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("For daily subscription, the start date must be same as the end date.");

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setSubscriptionType(SubscriptionType.WEEKLY)
                        .setStartDate(formatDate(endDate))
                        .setEndDate(formatDate(startDate))
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("For weekly and monthly subscription, the end date must be after the start date.");

        final LocalDate newEndDate = startDate.plusDays(15);

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setSubscriptionType(SubscriptionType.WEEKLY)
                        .setStartDate(formatDate(startDate))
                        .setEndDate(formatDate(newEndDate))
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("For weekly subscription, the day of week of end date must be the same as the start date (" +
                        startDate.toString() + " - " + startDate.getDayOfWeek() + ").");

        assertThatThrownBy(() ->
                Subscription.newBuilder()
                        .setSubscriptionType(SubscriptionType.MONTHLY)
                        .setStartDate(formatDate(startDate))
                        .setEndDate(formatDate(newEndDate))
                        .build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("For monthly subscription, the day of month of end date must be the same as the start date (" +
                        startDate.toString() + " - " + startDate.getDayOfMonth() + ").");
    }

    private String formatDate(final LocalDate date) {
        return date.format(Subscription.DATE_FORMAT);
    }
}
