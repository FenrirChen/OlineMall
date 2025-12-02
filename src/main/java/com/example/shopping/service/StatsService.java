package com.example.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.shopping.entity.Orders;
import com.example.shopping.entity.Product;
import com.example.shopping.mapper.OrdersMapper;
import com.example.shopping.mapper.ProductMapper;
import com.example.shopping.mapper.UserMapper;
import jakarta.servlet.ServletOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ProductMapper productMapper;

    // 1. 获取总销售额
    public BigDecimal getTotalSales() {
        List<Orders> orders = ordersMapper.selectList(null);
        if (orders == null || orders.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orders.stream()
                .map(Orders::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // 2. 获取总订单数
    public Long getTotalOrdersCount() {
        return ordersMapper.selectCount(null);
    }

    // 3. 获取总用户数
    public Long getTotalUsersCount() {
        return userMapper.selectCount(null);
    }

    // 4. 获取热销商品
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Product> products = productMapper.selectList(new QueryWrapper<Product>()
                .orderByAsc("stock")
                .last("LIMIT " + limit));

        List<Map<String, Object>> result = new ArrayList<>();
        if (products != null) {
            for (Product p : products) {
                int initialStock = 100;
                int sold = initialStock - (p.getStock() == null ? 0 : p.getStock());
                result.add(Map.of("name", p.getName(), "count", Math.max(sold, 0)));
            }
        }
        return result;
    }

    // 5. 获取近7天真实销售报表
    public List<Map<String, Object>> getRecentSalesStats(int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Orders> orders = ordersMapper.selectList(new QueryWrapper<Orders>()
                .ge("create_time", startDate.atStartOfDay()));

        List<Map<String, Object>> report = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);

            List<Orders> dayOrders = orders.stream()
                    .filter(o -> o.getCreateTime().toLocalDate().equals(date))
                    .collect(Collectors.toList());

            BigDecimal daySales = dayOrders.stream()
                    .map(Orders::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int dayOrderCount = dayOrders.size();

            Map<String, Object> stat = new HashMap<>();
            stat.put("date", date.toString());
            stat.put("orders", dayOrderCount);
            stat.put("sales", daySales);

            report.add(stat);
        }
        return report;
    }

    // 6. 新增：生成 Excel 文件
    public void exportDailyReport(ServletOutputStream outputStream) throws IOException {
        // 复用上面的逻辑获取数据
        List<Map<String, Object>> data = getRecentSalesStats(7);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("近7日销售报表");

            // 创建表头
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("日期");
            header.createCell(1).setCellValue("订单量");
            header.createCell(2).setCellValue("销售额 (元)");

            // 填充数据
            int rowIdx = 1;
            for (Map<String, Object> map : data) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue((String) map.get("date"));
                row.createCell(1).setCellValue(map.get("orders").toString());

                BigDecimal sales = (BigDecimal) map.get("sales");
                row.createCell(2).setCellValue(sales.doubleValue());
            }

            // 自动调整列宽
            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(2, 4000);

            workbook.write(outputStream);
        }
    }
}