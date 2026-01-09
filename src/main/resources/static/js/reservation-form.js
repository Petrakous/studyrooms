// static/js/reservation-form.js
// This script manages the reservation form's time inputs,
// enforcing a maximum 2-hour reservation duration.

document.addEventListener("DOMContentLoaded", function () {
    // Get references to the time input elements
    const startInput = document.getElementById("startTime");
    const endInput = document.getElementById("endTime");

    // Exit early if either input element doesn't exist on the page
    if (!startInput || !endInput) {
        return;
    }

    // Listen for changes to the start time input
    startInput.addEventListener("change", function () {
        // If start time is empty, do nothing
        if (!startInput.value) return;

        // Parse the time string (format "HH:mm") into hours and minutes
        const [h, m] = startInput.value.split(":").map(Number);
        
        // Create a Date object and set it to the selected start time
        const date = new Date();
        date.setHours(h, m || 0, 0, 0);

        // Add 2 hours to calculate the maximum allowed end time
        // This enforces a 2-hour maximum reservation duration
        date.setHours(date.getHours() + 2);

        // Format hours and minutes with leading zeros (e.g., "09:05")
        const endHours = String(date.getHours()).padStart(2, "0");
        const endMinutes = String(date.getMinutes()).padStart(2, "0");

        // Create the formatted time string for the max end time
        const maxValue = `${endHours}:${endMinutes}`;

        // Set the maximum allowed value for the end time input
        // This prevents users from selecting an end time more than 2 hours after start
        endInput.max = maxValue;

        // Auto-fill the end time if the user hasn't selected one yet
        // Defaults to the maximum allowed time (start + 2 hours)
        if (!endInput.value) {
            endInput.value = maxValue;
        }
    });
});
