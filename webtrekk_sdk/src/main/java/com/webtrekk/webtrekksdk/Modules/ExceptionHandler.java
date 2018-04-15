/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Webtrekk GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Created by Arsen Vartbaronov on 08.04.16.
 */

package com.webtrekk.webtrekksdk.Modules;

import android.content.Context;

import com.webtrekk.webtrekksdk.Request.RequestFactory;
import com.webtrekk.webtrekksdk.Request.TrackingRequest;
import com.webtrekk.webtrekksdk.Configuration.TrackingConfiguration;
import com.webtrekk.webtrekksdk.TrackingParameter.Parameter;
import com.webtrekk.webtrekksdk.TrackingParameter;
import com.webtrekk.webtrekksdk.Utils.WebtrekkLogging;
import com.webtrekk.webtrekksdk.Webtrekk;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Class is required to handle exception
 */
public class ExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private Thread.UncaughtExceptionHandler mOldHandler;
    private static int MAX_PARAMETER_NUMBER = 255;
    private RequestFactory mRequestFactory;
    private Context mContext;
    private static final String EX_ITEM_SEPARATE = "wte_item";
    private static final String START_EX_STRING = "wte_start";
    private static final String END_EX_STRING = "wte_end";
    private static final String EX_LINE_SEPARATOR = "|";

    enum Type
    {
        NON_DEFINED,
        FATAL,
        CATCHED,
        INFO
    }

    static class IncorrectErrorFileFormatException extends Exception{
        IncorrectErrorFileFormatException(String message)
        {
            super(message);
        }
    };

    public void init(RequestFactory requestFactory, Context context)
    {
        mRequestFactory = requestFactory;
        mContext = context;

        if (isLevelAllowed(Type.FATAL)) {
            mOldHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
            loadAndTrack();
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        WebtrekkLogging.log("caught uncatched exception");
        save(ex);

       if (mOldHandler != null)
           mOldHandler.uncaughtException(thread, ex);
    }

    /**
     * Instant track of exception this is for catched exception
     * @param ex
     */
    public void trackCatched(Throwable ex)
    {
        if (!isLevelAllowed(Type.CATCHED))
            return;

        if (ex == null)
        {
            WebtrekkLogging.log("exception is null nothing to track");
            return;
        }

        track(Type.CATCHED.ordinal(), getExceptionName(ex), getExceptionMessage(ex), getExceptionMessage(ex.getCause()),
                ex.getStackTrace() == null ? null : getExceptionStackString(ex.getStackTrace(), EX_LINE_SEPARATOR),
                ex.getCause() == null ? null : getExceptionStackString(ex.getCause().getStackTrace(), EX_LINE_SEPARATOR));
    }

    /**
     * Track info message
     * @param name
     * @param message
     */
    public void trackInfo(String name, String message)
    {
        if (!isLevelAllowed(Type.INFO))
            return;

        track(Type.INFO.ordinal(), name, message, null, null, null);
    }

    /**
     * Instant track of exception stack and causeStack can be null
     * @param type of exception
     * @param name of exception
     * @param message of exception can be null
     * @param stack of exception can be null
     * @param causeStack of cause stack of exception can be null
     */
    private void track(int type, String name, String message, String causeMessage, String stack, String causeStack)
    {
        TrackingParameter trackingParameter = new TrackingParameter();

        if ((type < 1 && type > 3) || name == null ) {
            WebtrekkLogging.log("Exception track error: Message or Name or Type isn't valid. Please check that either Massage or Name isn't null and Type is valid.");
            return;
        }

        trackingParameter.add(Parameter.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        trackingParameter.add(Parameter.ACTION_NAME, "webtrekk_ignore");
        trackingParameter.add(Parameter.ACTION, "910", String.valueOf(type));
        trackingParameter.add(Parameter.ACTION, "911", normalizeField(name));

        if (message != null)
           trackingParameter.add(Parameter.ACTION, "912", normalizeField(message));

        if (causeMessage != null)
            trackingParameter.add(Parameter.ACTION, "913", normalizeField(causeMessage));

        if (stack != null)
          trackingParameter.add(Parameter.ACTION, "914", normalizeField(stack));

        if (causeStack != null)
          trackingParameter.add(Parameter.ACTION, "915", normalizeField(causeStack));

        TrackingRequest request = new TrackingRequest(trackingParameter, mRequestFactory.getTrackingConfiguration(), TrackingRequest.RequestType.ECXEPTION);
        mRequestFactory.addRequest(request);
     }

    private void loadAndTrack()
    {
        String fileName = getFileName(false);
        File loadFile = new File(fileName);

        if (loadFile.exists())
        {
            //dumpFile();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(fileName));
                String line = null;

                while ((line = br.readLine()) != null) {
                    if (!line.equals(START_EX_STRING))
                        throw new IncorrectErrorFileFormatException("no start item");
                    ExceptionFileReader exReader = new ExceptionFileReader(br);
                    exReader.read();

                    track(Type.FATAL.ordinal(), exReader.getName(), exReader.getMessage(),
                            exReader.getCauseMessage(), exReader.getStack(),
                            exReader.getCauseStack());
                }
            }catch (IncorrectErrorFileFormatException e)
            {
                WebtrekkLogging.log("Incorrect File Exception Format:" + e);
            }catch (FileNotFoundException e) {
                WebtrekkLogging.log("Can't read exception file:"+e);
            } catch (IOException e) {
                WebtrekkLogging.log("Can't read exception file:" + e);
            }finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (loadFile.delete())
                    WebtrekkLogging.log("File "+loadFile.getName()+" delete success");
            }

        }
    }

    private void save(Throwable ex)
    {
        if (ex == null)
        {
            WebtrekkLogging.log("exception is null nothing to save");
            return;
        }

        BufferedOutputStream outputStream = null;

        String[] arrayToSave = {START_EX_STRING, getExceptionName(ex), EX_ITEM_SEPARATE, getExceptionMessage(ex),
                EX_ITEM_SEPARATE, ex.getCause() == null ? null : getExceptionMessage(ex.getCause()), EX_ITEM_SEPARATE,
                getExceptionStackString(ex.getStackTrace(), "\n"), EX_ITEM_SEPARATE,
                ex.getCause() == null ? null:getExceptionStackString(ex.getCause().getStackTrace(),"\n"), EX_ITEM_SEPARATE,
                END_EX_STRING};

        try {
            outputStream = new BufferedOutputStream(mContext.openFileOutput(getFileName(true), Context.MODE_APPEND));
            for (String string: arrayToSave){

                if (string != null) {
                    outputStream.write(string.getBytes());
                }
                outputStream.write("\n".getBytes());

            }
            outputStream.flush();
            WebtrekkLogging.log("Exception saved to file");
        } catch (Exception e) {
            WebtrekkLogging.log("can't save exception to file: "+e);
        }finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private String getExceptionStackString(StackTraceElement[] stack, String separator)
    {
        String stackString = "";

        if (stack == null)
            return null;

        for (StackTraceElement element: stack)
        {
            if (!stackString.isEmpty())
                stackString += separator;

            int lineNumber = (element.getClassName().contains("android.app.") || element.getClassName().contains("java.lang.")) ? -1 : element.getLineNumber();

            String stackItem = element.getClassName() + "."+
                    element.getMethodName()+"("+element.getFileName();

            if (lineNumber < 0)
                stackItem += ")";
             else
                stackItem += ":"+element.getLineNumber()+")";

            if (stackString.length() + stackItem.length() <= MAX_PARAMETER_NUMBER)
               stackString += stackItem;
            else
               break;
        }

        return stackString;
    }

    private String getExceptionName(Throwable ex)
    {
        return ex.getClass().getName();
    }

    private String getExceptionMessage(Throwable ex)
    {
        return ex == null ? null : ex.getMessage();
    }

    private String getFileName(boolean fileOnly)
    {
        return fileOnly ? "exception.txt" : mContext.getFilesDir().getPath() + File.separator+"exception.txt";
    }

    /**
     * Normalize field and make it less then 255
     * @param value
     * @return
     */
    private String normalizeField(String value){

        if (value == null)
            return null;

        if (value.length() > 255){

            WebtrekkLogging.log("string is more then 255 length during excception tracking. Normalize it by cutting to 255 length.");

            return value.substring(0, 255);
        }else
            return value;
    }

    /**
     * For debug purpose only
     */
    private void dumpFile()
    {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(getFileName(false)));
        String line = null;
        WebtrekkLogging.log("file ex dump begin ---------------------------------------------");
        while ((line = br.readLine()) != null) {
            WebtrekkLogging.log(line);
        }
        WebtrekkLogging.log("file ex dump end ---------------------------------------------");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isLevelAllowed(Type type)
    {
        TrackingConfiguration config = mRequestFactory.getTrackingConfiguration();

        return (config.isErrorLogEnable() && config.getErrorLogLevel() >= type.ordinal());
    }


    private static class ExceptionFileReader{
        private final BufferedReader mBuffer;
        private String mName;
        private String mMessage;
        private String mStack;
        private String mCauseStack;
        private String mCauseMessage;

        public String getCauseStack() {
            return mCauseStack.isEmpty() ? null: mCauseStack;
        }

        public String getName() {
            return mName;
        }

        public String getMessage() {
            return mMessage.isEmpty() ? null: mMessage;
        }

        public String getCauseMessage() {
            return mCauseMessage.isEmpty() ? null : mCauseMessage;
        }

        public String getStack() {
            return mStack .isEmpty() ? null: mStack;
        }

        public ExceptionFileReader(BufferedReader buffer)
        {
          mBuffer = buffer;
        }

        /**
         * Before calling this function it should be checked for START_EX_STRING
         * @throws IOException
         * @throws IncorrectErrorFileFormatException
         */
        public void read() throws IOException, IncorrectErrorFileFormatException {
            mName = readLine(mBuffer);
            validateString(mBuffer, EX_ITEM_SEPARATE, "no name-message item separated");
            mMessage = readLine(mBuffer);
            validateString(mBuffer, EX_ITEM_SEPARATE, "no message-cause message item separated");
            mCauseMessage = readLine(mBuffer);
            validateString(mBuffer, EX_ITEM_SEPARATE, "no cause message-stack item separated");
            mStack = readStack(mBuffer);
            mCauseStack = readStack(mBuffer);
            validateString(mBuffer, END_EX_STRING, "Can't find end string");
        }

        private String readLine(BufferedReader buffer) throws IncorrectErrorFileFormatException, IOException {
            String line = buffer.readLine();

            if (line == null)
                throw new IncorrectErrorFileFormatException("Unexpected null line");
            return line;
        }

        private void validateString(BufferedReader buffer, String toCheck, String errorExeption) throws IOException, IncorrectErrorFileFormatException {
            String line = readLine(buffer);
            if (!line.equals(toCheck))
                throw new IncorrectErrorFileFormatException(errorExeption);
        }

        private String readStack(BufferedReader buffer) throws IncorrectErrorFileFormatException, IOException
        {
            String line, exception = "";

            while(!(line = readLine(buffer)).equals(EX_ITEM_SEPARATE))
            {
                if (!exception.isEmpty())
                   exception += EX_LINE_SEPARATOR;
                exception += line;
            }
            return exception;
        }
    }

}
