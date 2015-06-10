package org.motechproject.nms.csv.utils;

import org.supercsv.comment.CommentMatcher;

/**
 * Implementation of CommentMatcher, used for skipping prefix lines in the FLW, Mother, and Child CSV files from
 * the government data service
 */
public class DataServiceCommentMatcher implements CommentMatcher {

    public boolean isComment(String line) {

        return isNullOrBlank(line) || line.contains(" :\t");
    }

    private boolean isNullOrBlank(String param) {
        return param == null || param.trim().length() == 0;
    }
}
