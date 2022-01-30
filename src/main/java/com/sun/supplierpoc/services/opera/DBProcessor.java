package com.sun.supplierpoc.services.opera;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.Conversions;
import com.sun.supplierpoc.components.NewBookingExcelHelper;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.opera.booking.Generate;
import com.sun.supplierpoc.models.opera.booking.Package;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class DBProcessor {
    @Autowired
    NewBookingExcelHelper bookingExcelHelper;

    Conversions conversions = new Conversions();

//    AccountCredential accountCredential
    public JdbcTemplate createConnection() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setUrl("jdbc:oracle:thin:@192.168.1.18:1521/opera");
        dataSource.setUsername("opera");
        dataSource.setPassword("opera");

        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");

        JdbcTemplate template = new JdbcTemplate(dataSource);

        return template;
    }

    public ArrayList<Package> getReservationPackage(String reservationId, int adults, int children, int noOfRooms){
        ArrayList<Package> packages = new ArrayList<>();
        JdbcTemplate template = createConnection();

        List<Map<String, Object>> rows;
        String selectQuery =
                "select " +
                        "       reservation_product_prices.product," +
                        "       ( reservation_product_prices.price * reservation_product_prices.quantity ) AS prod_price," +
                        "       reservation_product_prices.calculation_rule," +
                        "       reservation_product_prices.consumption_date," +

                        "       CASE" +
                        "         WHEN trx$_codes.description IS NULL THEN '-' ELSE trx$_codes.description " +
                        "       END as description," +

                        "       CASE" +
                        "         WHEN trx_class_relationships.percentage IS NULL THEN 0 ELSE trx_class_relationships.percentage " +
                        "       END as percentage," +
                        "       CASE" +
                        "         WHEN trx_class_relationships.percentage_base_code IS NULL THEN 0 ELSE trx_class_relationships.percentage_base_code " +
                        "       END as percentage_base_code " +
                        "From  reservation_daily_element_name " +
                        "       LEFT JOIN reservation_product_prices " +
                        "              ON (reservation_product_prices.resv_name_id  = '" + reservationId + "' " +
                        "                   AND reservation_daily_element_name.reservation_date = " +
                        "                       reservation_product_prices.consumption_date ) " +
                        "       LEFT JOIN product_posting_rules " +
                        "              ON reservation_product_prices.product = " +
                        "                 product_posting_rules.product " +
                        "       LEFT JOIN trx_class_relationships " +
                        "              ON product_posting_rules.trx_code = " +
                        "                 trx_class_relationships.trx_code_generator " +
                        "       LEFT JOIN trx$_codes " +
                        "              ON  trx_class_relationships.trx_code = trx$_codes.trx_code " +
                        "WHERE  reservation_daily_element_name.resv_name_id = '" + reservationId + "' " +
                        "order by reservation_daily_element_name.reservation_date, " +
                        "          reservation_product_prices.product," +
                        "          reservation_product_prices.PRODUCT_SOURCE," +
                        "          trx_class_relationships.calculation_sequence";

        rows = template.queryForList(selectQuery);

        Map<String, Object> row;
        Map<String, Object> subRow;
        Package pkg;
        Generate generate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        for (int i = 0; i < rows.size(); i++) {
            row = rows.get(i);
            pkg = new Package();
            if(rows.get(i).get("product") == null)
                continue;

            pkg.packageName = (String) row.get("product");
            pkg.price = ((BigDecimal) row.get("prod_price")).doubleValue();
            pkg.calculationRule = (String) row.get("calculation_rule");
            pkg.consumptionDate = (Date) row.get("consumption_date");

            switch (pkg.calculationRule) {
                case "A":
                    pkg.price = pkg.price * adults;
                    break;
                case "C":
                    pkg.price = pkg.price * children;
                    break;
                case "R":
                    pkg.price = pkg.price * noOfRooms;
                    break;
            }

            // Read generates
            for (int j = i; j < rows.size(); j++) {
                subRow = rows.get(j);
                generate = new Generate();
                generate.packageName = (String) subRow.get("product");
                generate.description = (String) subRow.get("description");
                generate.percentage = ((BigDecimal) subRow.get("percentage")).intValue();
                generate.percentageBaseCode = ((BigDecimal) subRow.get("percentage_base_code")).intValue();
                generate.consumptionDate = (Date) row.get("consumption_date");

                if(!pkg.packageName.equals(generate.packageName) || pkg.consumptionDate.compareTo(generate.consumptionDate) != 0){
                    i = j-1;
                    break;
                }

                pkg.generates.add(generate);
            }


            // Check if package already exists
            if (!conversions.checkPackageExistence(packages, pkg.packageName, pkg.source, pkg.consumptionDate)) {
                // Calculate Taxes
                bookingExcelHelper.calculatePackageTax(pkg);
                packages.add(pkg);
            }
        }
        return packages;
    }

}
