package com.example.shopping.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shopping.entity.*;
import com.example.shopping.mapper.OrderItemMapper;
import com.example.shopping.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService extends ServiceImpl<OrdersMapper, Orders> {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private LogService logService;

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(User user, Orders orderData) {
        // 1. 获取购物车
        List<CartItem> cartItems = cartService.getCartByUserId(user.getId());
        if (cartItems == null || cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 2. 校验库存并计算总价
        for (CartItem item : cartItems) {
            Product product = productService.getById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足");
            }
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 3. 保存订单
        orderData.setUserId(user.getId());
        orderData.setTotalAmount(totalAmount);
        orderData.setStatus("PENDING");
        orderData.setCreateTime(LocalDateTime.now());
        this.save(orderData);

        // 4. 保存明细 & 扣库存
        for (CartItem item : cartItems) {
            Product product = productService.getById(item.getProductId());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(orderData.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItemMapper.insert(orderItem);

            product.setStock(product.getStock() - item.getQuantity());
            productService.updateById(product);
        }

        // 5. 清购物车
        cartService.clearCart(user.getId());

        // 6. 记录日志
        logService.record(user.getUsername(), "ORDER", "创建订单 #" + orderData.getId());

        // 7. 发送邮件
        sendOrderEmail(user.getEmail(), orderData);
    }

    private void sendOrderEmail(String toEmail, Orders order) {
        if (mailSender == null || toEmail == null) return;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // 关键修改：必须与 SMTP 认证账号一致
            message.setFrom("sendmail@fenrirchen.com");
            message.setTo(toEmail);
            message.setSubject("【电子商城】订单确认通知 - #" + order.getId());
            message.setText("尊敬的用户：\n\n您的订单已成功提交！\n\n" +
                    "订单号：" + order.getId() + "\n" +
                    "支付金额：¥" + order.getTotalAmount() + "\n" +
                    "收货人：" + order.getReceiverName() + "\n" +
                    "收货地址：" + order.getAddress() + "\n\n" +
                    "我们会尽快为您安排发货。\n" +
                    "感谢您的光临！");

            mailSender.send(message);
            System.out.println("邮件发送成功至: " + toEmail);
        } catch (Exception e) {
            // 仅打印错误，不回滚订单
            System.err.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Orders> getByUserId(Long userId) {
        QueryWrapper<Orders> query = new QueryWrapper<>();
        query.eq("user_id", userId).orderByDesc("create_time");
        return this.list(query);
    }

    public List<Orders> findAll(String status) {
        QueryWrapper<Orders> query = new QueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq("status", status);
        }
        query.orderByDesc("create_time");
        return this.list(query);
    }

    public void shipOrder(Long orderId) {
        Orders order = this.getById(orderId);
        if (order != null) {
            order.setStatus("SHIPPED");
            this.updateById(order);
        }
    }
}