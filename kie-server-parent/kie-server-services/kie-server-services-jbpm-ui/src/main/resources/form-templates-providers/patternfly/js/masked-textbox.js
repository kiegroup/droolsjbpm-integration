/**
 * MaskedTextBox functionality for jBPM forms
 * Based on Business Central masked-input.js implementation
 * Handles client-side masking of text input values
 */
(function() {
    'use strict';

    // Initialize masked input fields when the page loads
    document.addEventListener('DOMContentLoaded', function() {
        initializeMaskedInputFields();
    });

    // Also initialize when new content is added dynamically
    if (typeof MutationObserver !== 'undefined') {
        var observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.type === 'childList') {
                    mutation.addedNodes.forEach(function(node) {
                        if (node.nodeType === 1) { // Element node
                            initializeMaskedInputFields(node);
                        }
                    });
                }
            });
        });
        
        observer.observe(document.body, {
            childList: true,
            subtree: true
        });
    }

    function initializeMaskedInputFields(container) {
        container = container || document;
        
        // Find all input fields that should be masked
        var inputs = container.querySelectorAll('input[data-field-type="MaskedTextBox"], input.masked-input-text-field, input.masked-textbox');
        
        inputs.forEach(function(input) {
            if (!input.hasAttribute('data-masked-initialized')) {
                setupMaskedInput(input);
                input.setAttribute('data-masked-initialized', 'true');
            }
        });
    }

    function setupMaskedInput(input) {
        var originalValue = input.value || '';
        var isMasked = false;
        
        // Get masking configuration from data attributes
        var maskingCharacter = input.getAttribute('data-masking-character') || input.getAttribute('data-masking-char') || '*';
        var maskingStartIndexAttr = input.getAttribute('data-masking-start-index');
        var maskingStartIndex = maskingStartIndexAttr !== null ? parseInt(maskingStartIndexAttr) : null;
        var maskingFromStartLengthAttr = input.getAttribute('data-masking-from-start-length');
        var maskingFromStartLength = maskingFromStartLengthAttr !== null ? parseInt(maskingFromStartLengthAttr) : null;
        var maskingFromEndLengthAttr = input.getAttribute('data-masking-from-end-length');
        var maskingFromEndLength = maskingFromEndLengthAttr !== null ? parseInt(maskingFromEndLengthAttr) : null;
        var isMaskedInDB = input.getAttribute('data-is-masked-in-db') === 'true' || input.getAttribute('data-masked-in-db') === 'true';
        
        // Apply initial masking if configured
        if (originalValue && (isMaskedInDB || input.readOnly)) {
            input.value = applyMasking(originalValue, maskingCharacter, maskingStartIndex, maskingFromStartLength, maskingFromEndLength);
            isMasked = true;
            input.classList.add('masked');
        }
        
        // Store original value
        input.setAttribute('data-original-value', originalValue);
        
        // Add event listeners
        input.addEventListener('focus', function() {
            if (!input.readOnly) {
                showOriginalValue();
            }
        });
        
        input.addEventListener('blur', function() {
            if (!input.readOnly) {
                applyMaskingToInput();
            }
        });
        
        input.addEventListener('input', function() {
            if (!input.readOnly) {
                // Update stored original value
                input.setAttribute('data-original-value', input.value);
            }
        });
        
        // Handle form submission to send original value if needed
        var form = input.closest('form');
        if (form) {
            form.addEventListener('submit', function() {
                if (!isMaskedInDB) {
                    // Send unmasked value to server
                    input.value = input.getAttribute('data-original-value') || '';
                }
            });
        }
        
        function showOriginalValue() {
            var original = input.getAttribute('data-original-value');
            if (original) {
                input.value = original;
                isMasked = false;
                input.classList.remove('masked');
                input.classList.add('unmasked');
            }
        }
        
        function applyMaskingToInput() {
            var original = input.getAttribute('data-original-value');
            if (original) {
                input.value = applyMasking(original, maskingCharacter, maskingStartIndex, maskingFromStartLength, maskingFromEndLength);
                isMasked = true;
                input.classList.add('masked');
                input.classList.remove('unmasked');
            }
        }
    }

    function applyMasking(value, maskingCharacter, maskingStartIndex, maskingFromStartLength, maskingFromEndLength) {
        if (!value) {
            return value;
        }
        
        var maskedValue = value;
        
        // Apply masking from start index
        if (maskingStartIndex !== null && maskingStartIndex !== undefined && maskingFromStartLength !== null && maskingFromStartLength !== undefined) {
            var startIndex = Math.min(maskingStartIndex, value.length);
            var endIndex = Math.min(startIndex + maskingFromStartLength, value.length);
            
            maskedValue = value.substring(0, startIndex) + 
                         maskingCharacter.repeat(endIndex - startIndex) + 
                         value.substring(endIndex);
        }
        
        // Apply masking from end
        if (maskingFromEndLength !== null && maskingFromEndLength !== undefined && maskingFromEndLength > 0) {
            var startIndex = Math.max(0, maskedValue.length - maskingFromEndLength);
            maskedValue = maskedValue.substring(0, startIndex) + 
                         maskingCharacter.repeat(maskedValue.length - startIndex);
        }
        
        return maskedValue;
    }

    // Expose functions globally for manual initialization
    window.initializeMaskedInputFields = initializeMaskedInputFields;
    window.MaskedTextBox = {
        applyMasking: applyMasking,
        initializeMaskedInputFields: initializeMaskedInputFields,
        reinitialize: function() {
            initializeMaskedInputFields();
        }
    };

})();
