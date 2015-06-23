package org.motechproject.nms.region.csv;

import java.io.IOException;
import java.io.Reader;

public interface LocationDataImportService {

    void importData(Reader reader) throws IOException;
}
