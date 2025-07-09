// Custom price slider implementation using HTML5 range inputs
function initPriceSlider(minVal, maxVal) {
  const sliderContainer = document.getElementById("priceSlider");
  if (!sliderContainer) {
    console.error("Price slider container not found");
    return;
  }

  // Clear existing content
  sliderContainer.innerHTML = "";

  const DEFAULT_MAX = 30000000;
  const min = minVal || 0;
  const max = maxVal || DEFAULT_MAX;

  // Create the slider HTML
  sliderContainer.innerHTML = `
        <div class="price-slider-container">
            <div class="range-inputs">
                <input type="range" id="minRange" class="range-slider" min="0" max="${DEFAULT_MAX}" value="${min}" step="100000">
                <input type="range" id="maxRange" class="range-slider" min="0" max="${DEFAULT_MAX}" value="${max}" step="100000">
            </div>
            <div class="range-track"></div>
        </div>
    `;

  const minRange = document.getElementById("minRange");
  const maxRange = document.getElementById("maxRange");
  const minInput = document.getElementById("filtersForm:minPrice");
  const maxInput = document.getElementById("filtersForm:maxPrice");
  const minLabel = document.getElementById("minValueLabel");
  const maxLabel = document.getElementById("maxValueLabel");

  if (!minRange || !maxRange) {
    console.error("Range inputs not found");
    return;
  }

  // Update hidden inputs and labels
  function updateValues() {
    const minValue = parseInt(minRange.value);
    const maxValue = parseInt(maxRange.value);

    if (minInput) minInput.value = minValue;
    if (maxInput) maxInput.value = maxValue;
    if (minLabel) minLabel.innerText = minValue.toLocaleString();
    if (maxLabel) maxLabel.innerText = maxValue.toLocaleString();

    // Update track styling
    updateTrack();
  }

  // Update the track styling
  function updateTrack() {
    const minValue = parseInt(minRange.value);
    const maxValue = parseInt(maxRange.value);
    const minPercent = (minValue / DEFAULT_MAX) * 100;
    const maxPercent = (maxValue / DEFAULT_MAX) * 100;

    const track = sliderContainer.querySelector(".range-track");
    if (track) {
      track.style.background = `linear-gradient(to right, #e0e0e0 0%, #e0e0e0 ${minPercent}%, #007bff ${minPercent}%, #007bff ${maxPercent}%, #e0e0e0 ${maxPercent}%, #e0e0e0 100%)`;
    }
  }

  // Event listeners
  minRange.addEventListener("input", function () {
    const minValue = parseInt(this.value);
    const maxValue = parseInt(maxRange.value);

    if (minValue > maxValue) {
      maxRange.value = minValue;
    }
    updateValues();
  });

  maxRange.addEventListener("input", function () {
    const minValue = parseInt(minRange.value);
    const maxValue = parseInt(this.value);

    if (maxValue < minValue) {
      minRange.value = maxValue;
    }
    updateValues();
  });

  // Initialize
  updateValues();

  // Force a small delay to ensure DOM is ready
  setTimeout(() => {
    updateTrack();
  }, 50);
}

// Re-initialize the slider after JSF AJAX updates or on page load
function refreshSliderAfterAjax() {
  setTimeout(() => {
    const min = parseInt(
      document.getElementById("filtersForm:minPrice")?.value || "0"
    );
    const max = parseInt(
      document.getElementById("filtersForm:maxPrice")?.value || "30000000"
    );
    initPriceSlider(min, max);
  }, 150); // Increased timeout to ensure DOM is fully updated
}

// Initialize on DOMContentLoaded
document.addEventListener("DOMContentLoaded", function () {
  console.log("DOM loaded, initializing price slider...");
  refreshSliderAfterAjax();
});

// Reinitialize after JSF AJAX events
if (window.jsf) {
  jsf.ajax.addOnEvent(function (data) {
    if (data.status === "success") {
      console.log("JSF AJAX success, refreshing slider...");
      refreshSliderAfterAjax();
    }
  });
}

// Fallback initialization for immediate execution
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", refreshSliderAfterAjax);
} else {
  refreshSliderAfterAjax();
}
