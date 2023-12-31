package mis.jewishlewish.demo;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AppController {

    @GetMapping
    String getApp(HttpServletRequest request) {

        try{
            if (WebUtils.getCookie(request, "firstname") == null || 
                WebUtils.getCookie(request, "lastname") == null || 
                WebUtils.getCookie(request, "uuid") == null) {
            return "redirect:/login";
            }

        } catch (Exception e) {
            return "redirect:/res";
        }
        return "redirect:/login";
    }

    @GetMapping("/res")
    String getRes(Model model) {
        List<json> questions = json.readJson();
        model.addAttribute("questions",questions);
        return "res";
    }

    @PostMapping("/res")
    public String processForm(@RequestParam Map<String, String> requestParams, Model model, HttpServletRequest request) {
        
        Py.print(requestParams.toString());

        Cookie[] cookies = request.getCookies();
        String uuid_str = get_uuid(cookies);
        if (uuid_str == null) {
            return "redirect:/login";
        }

        List<json> questions = json.readJson();
        int i = 0;
        HashMap<String, String> paramMap = new HashMap<>();
        
        for (Map.Entry<String, String> entry : requestParams.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
            paramMap.put(key, value);

            DataSQL.add_question(uuid_str, questions.get(i).getQuestion(), value);
            i++;
        }

        
        model.addAttribute("questions",questions);
        
        return "res";
    }

    private String get_uuid(Cookie[] cookies) {
        if (cookies != null) { for (Cookie cookie : cookies) { if (cookie.getName().equals("uuid")) { return (String) cookie.getValue(); } } }
        return null;
    }

    @GetMapping("/login")
    String getlogin(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String processlogin(@RequestParam("first_name") String firstName, @RequestParam("last_name") String lastName, HttpServletResponse response, Model model) {
        
        Py.print("First Name: " + firstName);
        Py.print("Last Name: " + lastName);

        String fullName = firstName + lastName;

        UUID uuid = UUID.nameUUIDFromBytes(fullName.getBytes());

        String randomId = uuid.toString();

        Cookie Cookies_firstname = new Cookie("firstname", firstName);
        Cookie Cookies_lastname = new Cookie("lastname", lastName);
        Cookie Cookies_uuid = new Cookie("uuid", randomId);

        response.addCookie(Cookies_firstname);
        response.addCookie(Cookies_lastname);
        response.addCookie(Cookies_uuid);

        DataSQL.add_user(firstName,lastName,randomId); //add data to SQL

        
        return "redirect:/res";
    }
}
