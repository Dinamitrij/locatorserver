package lv.div.locator.servlet;

import lv.div.locator.commons.conf.Const;
import lv.div.locator.conf.ConfigurationManager;
import lv.div.locator.dao.LogFileDao;
import lv.div.locator.healthcheck.AlertSender;
import lv.div.locator.model.LogFile;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

public class LogFileUpload extends HttpServlet {

    @EJB
    private LogFileDao logFileDao;

    private Logger log = Logger.getLogger(LogFileUpload.class.getName());

    @EJB
    private AlertSender alertSender;

    @EJB
    private ConfigurationManager configurationManager;

    private final int BUFFER_LENGTH = 4096;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                           IOException {

        log.info("LogFileUpload doPost() called.");
        LogFile logFile = new LogFile();

        try {
            List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                    String fieldName = item.getFieldName();

                    if (Const.DEVICE_ID_HTTP_PARAMETER.equals(fieldName)) {
                        logFile.setDeviceId(item.getString());
                    } else if (Const.DEVICE_ALIAS_HTTP_PARAMETER.equals(fieldName)) {
                        logFile.setDeviceName(item.getString());
                    } else if (Const.ZIPPED_LOG_FILENAME_PARAM.equals(fieldName)) {
                        logFile.setFilename(item.getString());
                    }
                } else {
                    // Process form file field (input type="file").
                    String fieldName = item.getFieldName();
                    String fileName = FilenameUtils.getName(item.getName());
                    InputStream fileContent = item.getInputStream();

                    final byte[] bytes = IOUtils.toByteArray(fileContent);
                    final byte[] encodedBytes = Base64.encodeBase64(bytes);
                    logFile.setFiledata(new String(encodedBytes));
                    log.info(bytes.length + " bytes of data received.");

                    // NB: does not close inputStream, you can use IOUtils.closeQuietly for that
                    IOUtils.closeQuietly(fileContent);
                }
            }

            if (!StringUtils.isBlank(logFile.getFiledata())) {
                logFileDao.save(logFile);
            }

        } catch (Exception e) {
            throw new ServletException("Cannot parse/save multipart request.", e);
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
                                                                                          IOException {

        response.getWriter().print("...");
        response.getWriter().flush();
    }

}
