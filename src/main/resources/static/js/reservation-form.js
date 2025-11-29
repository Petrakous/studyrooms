// static/js/reservation-form.js

document.addEventListener("DOMContentLoaded", function () {
    const startInput = document.getElementById("startTime");
    const endInput = document.getElementById("endTime");

    if (!startInput || !endInput) {
        return;
    }

    startInput.addEventListener("change", function () {
        if (!startInput.value) return;

        // value μορφή "HH:mm"
        const [h, m] = startInput.value.split(":").map(Number);
        const date = new Date();
        date.setHours(h, m || 0, 0, 0);

        // +2 ώρες
        date.setHours(date.getHours() + 2);

        const endHours = String(date.getHours()).padStart(2, "0");
        const endMinutes = String(date.getMinutes()).padStart(2, "0");

        const maxValue = `${endHours}:${endMinutes}`;

        // βάζουμε max ώρα
        endInput.max = maxValue;

        // αν δεν έχει βάλει ακόμα end time, κάνε auto-fill
        if (!endInput.value) {
            endInput.value = maxValue;
        }
    });
});
