package com.gustz.beehive.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.util.*;

/**
 * sql builder
 *
 * @author zhangzhenfeng
 * @since 2016-03-10
 */
public abstract class SqlBuilder {

    private static final File INPUT_JSON_DIR = new File(SystemUtils.getUserDir() + "/doc/in/");

    private static final File OUTPUT_SQL_DIR = new File(SystemUtils.getUserDir() + "/doc/out/");

    private static String insertSqlTpl;

    private static String updateSqlTpl;

    private static int batchLimit;

    private static Properties props;

    /**
     * parse JSON file
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        // insert sql
        //createInsertSql();
        // update sql
        createUpdateSql();

    }

    static {
        InputStream ins = null;
        try {
            ins = Thread.currentThread().getContextClassLoader().getResourceAsStream("conf/sql-builder.properties");
            props = new Properties();
            props.load(ins);
            // insert sql tpl
            insertSqlTpl = props.getProperty("tpl.insert_sql");
            if (StringUtils.isBlank(insertSqlTpl) || !insertSqlTpl.contains("VALUES (") || !insertSqlTpl.contains(");")) {
                throw new Error("insert sql template invalid");
            }
            //　update sql tpl
            updateSqlTpl = props.getProperty("tpl.update_sql");
            if (StringUtils.isBlank(updateSqlTpl) || !updateSqlTpl.contains("UPDATE ") || !updateSqlTpl.contains("SET ")) {
                throw new Error("update sql template invalid");
            }
            // batch limit
            batchLimit = Integer.parseInt(props.getProperty("sql.batch_limit"));
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            if (ins != null) {
                try {
                    ins.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static List<String> getInsertSqlFields() {
        // get fields
        String field = insertSqlTpl.substring(insertSqlTpl.indexOf("(") + 1, insertSqlTpl.lastIndexOf(" VALUES") - 1);
        if (StringUtils.isBlank(field) || field.split(",").length == 0) {
            throw new Error("insert sql template invalid.[value field]");
        }
        return Arrays.asList(field.split(","));
    }

    private static List<String> getUpdateSqlFields() {
        // get fields
        String field = updateSqlTpl.substring(updateSqlTpl.indexOf("SET ") + 4, updateSqlTpl.lastIndexOf(" WHERE") + 1);
        if (StringUtils.isBlank(field)) {
            throw new Error("update sql template invalid.[value field]");
        }
        StringBuilder sbd = new StringBuilder();
        String[] fs = field.split(",");
        for (int i = 0; i < fs.length; i++) {
            String key = fs[i].split("=")[1];
            if (key == null || key.isEmpty() || !key.contains("#")) {
                continue;
            }
            key = key.replace("#", "").replace("'", "").trim();
            sbd.append(key).append(",");
        }
        String where = updateSqlTpl.substring(updateSqlTpl.indexOf("WHERE ") + 6, updateSqlTpl.lastIndexOf(";"));
        if (StringUtils.isBlank(where)) {
            throw new Error("update sql template invalid.[where field]");
        }
        String[] ws = where.split("AND");
        for (int i = 0; i < ws.length; i++) {
            if (i > 0) {
                sbd.append(",");
            }
            String key = ws[i].split("=")[1];
            if (key == null || key.isEmpty() || !key.contains("#")) {
                continue;
            }
            key = key.replace("#", "").replace("'", "").trim();
            sbd.append(key);
        }
        return Arrays.asList(sbd.toString().split(","));
    }

    private static List<Map<String, Object>> getJsonList(FileInputStream ins) throws IOException {
        String json = IOUtils.toString(ins, "utf-8");
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("input JSON file invalid");
        }
        Map map = new ObjectMapper().readValue(json, Map.class);
        if (map != null && map.size() > 0) {
            if (map.containsKey("records")) {
                return (List<Map<String, Object>>) map.get("records");
            } else if (map.containsKey("RECORDS")) {
                return (List<Map<String, Object>>) map.get("RECORDS");
            }
        }
        return null;
    }

    private static void createInsertSql() throws IOException {
        System.err.println("create insert sql file begin...");
        long bt = System.currentTimeMillis();
        try {
            // do write sql file
            writeSqlFile(insertSqlTpl, getInsertSqlFields());
        } finally {
            System.err.println("\ncreate insert sql file end,use time=" + (System.currentTimeMillis() - bt) + " ms.");
        }
    }

    private static void createUpdateSql() throws IOException {
        System.err.println("create update sql file begin...");
        long bt = System.currentTimeMillis();
        try {
            // do write sql file
            writeSqlFile(updateSqlTpl, getUpdateSqlFields());
        } finally {
            System.err.println("\ncreate update sql file end,use time=" + (System.currentTimeMillis() - bt) + " ms.");
        }
    }

    private static void writeSqlFile(final String sqlTpl, final List<String> fieldList) throws IOException {
        // get input json files
        File[] files = INPUT_JSON_DIR.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".json");
            }
        });
        int index = 0;
        for (File inputFile : files) {
            index++;
            System.err.println("\noutput file begin,index=" + index);
            // output sql file
            File outputSqlFile = new File(OUTPUT_SQL_DIR + "/" + inputFile.getName().replace(".json", "-out.sql"));
            // get json list
            List<Map<String, Object>> jsonList = getJsonList(new FileInputStream(inputFile));
            System.out.println("JSON data list size=" + (jsonList == null ? 0 : jsonList.size()));
            if (jsonList == null || jsonList.isEmpty()) {
                System.err.println("jsonList is empty.");
                return;
            }
            // get insert sql template
            System.out.println("field list size=" + fieldList.size());
            List sqlList = new ArrayList<>();
            // do create sql file
            int i = 0;
            final int size = jsonList.size();
            for (Map<String, Object> dataMap : jsonList) {
                if (dataMap == null || dataMap.isEmpty()) {
                    continue;
                }
                String sql = sqlTpl;
                if (i == 0) {
                    sql = "BEGIN;\n" + sql;
                }
                for (String field : fieldList) {
                    if (field == null || field.isEmpty()) {
                        throw new Error("field invalid.");
                    }
                    sql = sql.replace("#" + field, String.valueOf(dataMap.get(field)));
                }
                if (i > 0 && i % batchLimit == 0) {
                    sql += "\nCOMMIT;\nBEGIN;";
                    sqlList.add(sql);
                    FileUtils.writeLines(outputSqlFile, sqlList, true);
                    sqlList.clear();
                } else {
                    sqlList.add(sql);
                }
                i++;
            }
            if (sqlList.size() > 0) {
                sqlList.add("COMMIT;");
                FileUtils.writeLines(outputSqlFile, sqlList, true);
            }
            sqlList.clear();
            System.err.println("output file end,index=" + index);
        }
    }

}
