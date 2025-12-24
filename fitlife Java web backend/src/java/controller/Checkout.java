package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import entity.OrderItem;
import entity.OrderStatus;
import entity.ProductOrder;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.Mail;
import org.hibernate.Session;

@WebServlet(name = "Checkout", urlPatterns = {"/Checkout"})
public class Checkout extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("success", false);

        Gson gson = new Gson();
        JsonObject jsobject = gson.fromJson(request.getReader(), JsonObject.class);

        String firstname = jsobject.get("firstname").getAsString();
        String lastname = jsobject.get("lastname").getAsString();
        String email = jsobject.get("email").getAsString();
        String mobile = jsobject.get("mobile").getAsString();
        String address = jsobject.get("address").getAsString();
        String city = jsobject.get("city").getAsString();
        String country = jsobject.get("country").getAsString();

        // Extract cartList from JSON
        Type listType = new TypeToken<List<HashMap<String, String>>>() {
        }.getType();
        List<HashMap<String, String>> cartList = gson.fromJson(jsobject.get("cartlist"), listType);

        ProductOrder productOrder = new ProductOrder();
        productOrder.setDatetime(new Date());
        productOrder.setFirstname(firstname);
        productOrder.setLastname(lastname);
        productOrder.setEmail(email);
        productOrder.setMobile(mobile);
        productOrder.setAddress(address);
        productOrder.setCity(city);
        productOrder.setCountry(country);

        Session session = HibernateUtil.getSessionFactory().openSession();

        OrderStatus orderStatus = (OrderStatus) session.get(OrderStatus.class, 2);

        int order_id = (int) session.save(productOrder);
        session.beginTransaction().commit();

        for (HashMap<String, String> item : cartList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductid(item.get("pid"));
            orderItem.setTitle(item.get("title"));
            orderItem.setQty(item.get("qty"));
            orderItem.setPrice(item.get("price"));
            orderItem.setProductOrder(productOrder);
            orderItem.setOrderStatus(orderStatus);
            session.save(orderItem);
        }
        session.beginTransaction().commit();

        Thread sendMailThread = new Thread() {
            @Override
            public void run() {
                String subject = "Your Order Confirmation - FitLife";

                // Construct Email Content
                StringBuilder emailContent = new StringBuilder();
                emailContent.append("<!DOCTYPE html>");
                emailContent.append("<html lang='en'><head><meta name='viewport' content='width=device-width, initial-scale=1'>");
                emailContent.append("<style>");
                emailContent.append("body { font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px; }");
                emailContent.append(".container { background-color: #ffffff; max-width: 600px; margin: auto; padding: 20px; border-radius: 10px; box-shadow: 0px 0px 10px rgba(0,0,0,0.1); }");
                emailContent.append("h2 { color: #6482AD; text-align: center; }");
                emailContent.append(".details { padding: 10px; background: #f1f1f1; border-radius: 5px; margin-bottom: 10px; }");
                emailContent.append("table { width: 100%; border-collapse: collapse; }");
                emailContent.append("th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }");
                emailContent.append(".total { font-weight: bold; color: #ff5733; }");
                emailContent.append("</style>");
                emailContent.append("</head><body>");

                emailContent.append("<div class='container'>");
                emailContent.append("<h2>Thank you for your order!</h2>");

                // Billing & Delivery Details
                emailContent.append("<div class='details'><strong>Billing & Delivery Details:</strong><br>");
                emailContent.append(firstname).append(" ").append(lastname).append("<br>");
                emailContent.append(email).append("<br>");
                emailContent.append(mobile).append("<br>");
                emailContent.append(address).append(", ").append(city).append(", ").append(country);
                emailContent.append("</div>");

                // Order Items Table
                emailContent.append("<table>");
                emailContent.append("<tr><th>Product</th><th>Qty</th><th>Price</th><th>Total</th></tr>");

                double grandTotal = 0;
                for (HashMap<String, String> item : cartList) {
                    int qty = Integer.parseInt(item.get("qty"));
                    double price = Double.parseDouble(item.get("price"));
                    double total = qty * price;
                    grandTotal += total;

                    emailContent.append("<tr>");
                    emailContent.append("<td>").append(item.get("title")).append("</td>");
                    emailContent.append("<td>").append(qty).append("</td>");
                    emailContent.append("<td>Rs.").append(price).append("</td>");
                    emailContent.append("<td>Rs.").append(total).append("</td>");
                    emailContent.append("</tr>");
                }

                // Total Price
                emailContent.append("<tr><td colspan='3' class='total'>Grand Total</td><td class='total'>Rs.").append(grandTotal).append("</td></tr>");
                emailContent.append("</table>");

                emailContent.append("</div></body></html>");

                // Send the email
                Mail.sendMail(email, subject, emailContent.toString());
            }
        };
        sendMailThread.start();

        responseObject.addProperty("success", true);
        responseObject.addProperty("message", "Success");
        response.setContentType("application/json");
        response.getWriter().write(gson.toJson(responseObject));

    }

}
