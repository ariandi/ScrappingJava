package com.example.testbrick.service;

import com.example.testbrick.constant.StaticParams;
import com.example.testbrick.model.Product;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ExportCVS {

    public String setCVS(List<Product> products) {
        String filename = StaticParams.PRODUCT + StaticParams.UNDERSCORE + "handphone"
                + StaticParams.UNDERSCORE + System.currentTimeMillis() + StaticParams.CSV_EXT;

        CsvMapper csvMapper = new CsvMapper();
        csvMapper.enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
        csvMapper.addMixIn(Product.class, Product.ProductFormat.class);
        CsvSchema schema = csvMapper.schemaFor(Product.class).withHeader();

        try {
            File file = new File(filename);
            csvMapper.writer(schema).writeValue(file, products);
            return filename;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }

        return "error";
    }

}
