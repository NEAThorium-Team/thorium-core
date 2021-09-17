package com.neathorium.thorium.core.namespaces.clipboard;

import com.neathorium.thorium.core.constants.CoreDataConstants;
import com.neathorium.thorium.core.constants.clipboard.ClipboardConstants;
import com.neathorium.thorium.core.constants.validators.CoreFormatterConstants;
import com.neathorium.thorium.core.extensions.namespaces.CoreUtilities;
import com.neathorium.thorium.core.extensions.namespaces.NullableFunctions;
import com.neathorium.thorium.core.namespaces.DataFactoryFunctions;
import com.neathorium.thorium.core.namespaces.DataFunctions;
import com.neathorium.thorium.core.namespaces.ExceptionHandlers;
import com.neathorium.thorium.core.namespaces.exception.ClipboardExceptionHandlers;
import com.neathorium.thorium.core.namespaces.exception.ExceptionFunctions;
import com.neathorium.thorium.core.namespaces.predicates.DataPredicates;
import com.neathorium.thorium.core.namespaces.validators.clipboard.ClipboardValidators;
import com.neathorium.thorium.core.records.Data;
import com.neathorium.thorium.core.records.HandleResultData;
import com.neathorium.thorium.core.records.clipboard.ClipboardData;

import java.awt.datatransfer.StringSelection;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public interface ClipboardFunctions {
    private static Data<Boolean> copyToClipboardCore(ClipboardData<String> data, String message) {
        final var nameof = "copyToClipboard";
        final var errorMessage = ClipboardValidators.isValidClipboardData(data);
        if (isNotBlank(errorMessage)) {
            return DataFactoryFunctions.getInvalidBooleanWith(nameof, errorMessage);
        }

        final var setResult = ClipboardExceptionHandlers.setContentsHandler(data.clipboard, new StringSelection(message));
        if (DataPredicates.isInvalidOrFalse(setResult)) {
            return DataFactoryFunctions.replaceMessage(setResult, nameof, DataFunctions.getFormattedMessage(setResult));
        }

        final var getResult = getFromClipboardCore(data);
        final var exception = getResult.exception;
        final var status = isNotBlank(getResult.object) && ExceptionFunctions.isNonException(exception);
        return DataFactoryFunctions.getBoolean(status, nameof, "Copying(\"" + message + "\") to clipboard was " + (status ? "" : "un") + "successful" + CoreFormatterConstants.END_LINE, exception);
    }

    private static Data<String> getFromClipboardCore(ClipboardData<String> data) {
        final var nameof = "getFromClipboard";
        final var getResult = ClipboardExceptionHandlers.transferHandler(data);
        if (DataPredicates.isInvalidOrFalse(getResult)) {
            return DataFactoryFunctions.getWith(CoreFormatterConstants.EMPTY, getResult.status, nameof, DataFunctions.getFormattedMessage(getResult), getResult.exception);
        }

        final var castResult = ExceptionHandlers.classCastHandler(new HandleResultData<>(data.castData.CASTER, getResult.object, data.castData.DEFAULT_VALUE));
        return DataFactoryFunctions.replaceName(castResult, nameof);
    }

    static Function<String, Data<Boolean>> copyToClipboard() {
        return message -> NullableFunctions.isNotNull(message) ? copyToClipboardCore(ClipboardConstants.CLIPBOARD_DATA, message) : CoreDataConstants.NULL_BOOLEAN;
    }

    static Data<Boolean> copyToClipboard(String message) {
        return copyToClipboard().apply(message);
    }
}