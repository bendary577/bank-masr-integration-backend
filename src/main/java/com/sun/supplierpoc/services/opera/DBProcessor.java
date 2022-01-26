package com.sun.supplierpoc.services.opera;
import com.sun.supplierpoc.Constants;
import com.sun.supplierpoc.models.configurations.AccountCredential;
import com.sun.supplierpoc.models.opera.booking.Package;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class DBProcessor {
//    AccountCredential accountCredential
    public JdbcTemplate createConnection() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setUrl("jdbc:oracle:thin:@192.168.1.16:1521/opera");
        dataSource.setUsername("opera");
        dataSource.setPassword("opera");

        dataSource.setDriverClassName("oracle.jdbc.driver.OracleDriver");

        JdbcTemplate template = new JdbcTemplate(dataSource);

        return template;
    }

    public ArrayList<Package> getReservationPackage(String reservationId){
        ArrayList<Package> packages = new ArrayList<>();
        JdbcTemplate template = createConnection();

        List<Map<String, Object>> rows;
        rows = template.queryForList(
                "select " +
                        "       reservation_product_prices.product," +
                        "       ( reservation_product_prices.price * reservation_product_prices.quantity ) AS prod_price," +
                        "       reservation_product_prices.calculation_rule," +
                        "       reservation_product_prices.consumption_date," +

                        "       trx$_codes.description," +
                        "       trx_class_relationships.percentage," +
                        "       trx_class_relationships.percentage_base_code " +
                        "From  reservation_daily_element_name " +
                        "       LEFT JOIN reservation_product_prices " +
                        "              ON (reservation_product_prices.resv_name_id  = '24153' " +
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
                        "WHERE  reservation_daily_element_name.resv_name_id = '24153' " +
                        "order by reservation_daily_element_name.reservation_date, " +
                        "          reservation_product_prices.product," +
                        "          reservation_product_prices.PRODUCT_SOURCE," +
                        "          trx_class_relationships.calculation_sequence"
        );

        return packages;
    }

}
