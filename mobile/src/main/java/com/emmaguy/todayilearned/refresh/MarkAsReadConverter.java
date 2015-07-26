package com.emmaguy.todayilearned.refresh;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Scanner;

import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by emma on 14/06/15.
 */
public class MarkAsReadConverter implements Converter {
    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            Scanner s = new Scanner(body.in()).useDelimiter("\\A");
            String bodyText = s.hasNext() ? s.next() : "";

            boolean isSuccessResponse = bodyText.startsWith("202 Accepted");

            MarkAllRead markAllRead = new MarkAllRead();
            if (!isSuccessResponse) {
                markAllRead.setErrors(bodyText);
            }

            return markAllRead;
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    @Override
    public TypedOutput toBody(Object object) {
        throw new UnsupportedOperationException();
    }
}
