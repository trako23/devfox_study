package com.devfox.board.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devfox.board.config.UserInfo;
import com.devfox.board.model.member.LoginForm;
import com.devfox.board.model.member.Member;
import com.devfox.board.model.member.MemberJoinForm;
import com.devfox.board.repository.MemberMapper;
import com.devfox.board.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RequiredArgsConstructor
@RequestMapping("member")
@Controller
public class MemberController {
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private MemberService memberService;
	
	 // 회원가입 페이지 이동
    @GetMapping("join")
    public String joinForm(Model model) {
        // joinForm.html 의 필드 세팅을 위해 model 에 빈 MemberJoinForm 객체 생성하여 저장한다
        model.addAttribute("joinForm", new MemberJoinForm());
        // member/joinForm.html 페이지를 리턴한다.
        return "member/joinForm";
    }
 // 회원가입
    @PostMapping("join")
    public String join(@Validated @ModelAttribute("joinForm") MemberJoinForm joinForm,
                       BindingResult result,Model model, RedirectAttributes redirectAttributes) {
        log.info("joinForm: {}", joinForm);

        // validation 에 에러가 있으면 가입시키지 않고 member/joinForm.html 페이지로 돌아간다.
        if (result.hasErrors()) {
            return "member/joinForm";
        }
        
        // 사용자로부터 입력받은 아이디로 데이터베이스에서 Member 를 검색한다.
        //Member member = memberMapper.findMember(joinForm.getMember_id());
        // 사용자 정보가 존재하면
//        if (member != null) {
//        	model.addAttribute("member", member);
//            //log.info("이미 가입된 아이디 입니다.");
//            // BindingResult 객체에 GlobalError 를 추가한다.
//            result.reject("duplicate ID", messageSource.getMessage("alert.idcon", null, LocaleContextHolder.getLocale()));
//            // member/joinForm.html 페이지를 리턴한다.
//            return "member/joinForm";
//        }
        // MemberJoinForm 객체를 Member 타입으로 변환하여 데이터베이스에 저장한다.
        //memberMapper.saveMember(MemberJoinForm.toMember(joinForm));
        memberService.saveMember(MemberJoinForm.toMember(joinForm));
        //redirectAttributes.addFlashAttribute("alertMessage", messageSource.getMessage("alert.kaicon", null, LocaleContextHolder.getLocale()));
        // 로그인 페이지로 리다이렉트한다.
        return "redirect:/member/login";
    }
    
    
    // 로그인 페이지 이동
    @GetMapping("login")
    public String loginForm(@RequestParam(value="error", required = false) String error,
    						@RequestParam(value="exception",required = false) String exception
    						,Model model) {
        // member/loginForm.html 에 필드 셋팅을 위해 빈 LoginForm 객체를 생성하여 model 에 저장한다.
        model.addAttribute("loginForm", new LoginForm());
        /* 에러와 예외를 모델에 담아 view resolve */
		model.addAttribute("error", error);
		model.addAttribute("exception", exception);
        // member/loginForm.html 페이지를 리턴한다.
		log.info("error {}",exception);
        return "member/loginForm";
    }
    
    

    // 로그인 처리
    @PostMapping("login")
    public String login(@Validated @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult result,
                        HttpServletRequest request,
                        @RequestParam(defaultValue = "/") String redirectURL) {
        log.info("redirectURL: {}", redirectURL);
        log.info("loginForm: {}", loginForm);
        // validation 에 실패하면 member/loginForm 페이지로 돌아간다.
        if (result.hasErrors()) {
            return "member/loginForm";
        }
        // 사용자가 입력한 이이디에 해당하는 Member 정보를 데이터베이스에서 가져온다.
        Member member = memberMapper.findMember(loginForm.getMember_id());
        // Member 가 존재하지 않거나 패스워드가 다르면
//        if (member == null || !member.getPassword().equals(loginForm.getPassword())) {
//            // BindingResult 객체에 GlobalError 를 발생시킨다.
//            result.reject("loginError", messageSource.getMessage("alert.notcon", null, LocaleContextHolder.getLocale()));
//            // member/loginForm.html 페이지로 돌아간다.
//            return "member/loginForm";
//        }

        // Request 객체에서 Session 객체를 꺼내온다.
        HttpSession session = request.getSession();
        // Session 에 'loginMember' 라는 이름으로 Member 객체를 저장한다.
        session.setAttribute("loginMember", member);
        // 메인 페이지로 리다이렉트 한다.
        return "redirect:" + redirectURL;
    }
    
    @GetMapping("login-success")
    public String loginSuccess(@AuthenticationPrincipal UserInfo userInfo) {
    	log.info("로그인성공");
    	return "redirect:/";
    }
    
    @GetMapping("login-failed")
    public String loginFailed() {
    	log.info("로그인실패");
    	return "redirect:/";
    }
    
    // 로그아웃
    @GetMapping("logout")
    public String logout(HttpServletRequest request) {
        // Request 객체에서 Session 정보를 가져온다.
        HttpSession session = request.getSession(false);
        // 세션이 존재하면 세션의 모든 데이터를 리셋한다.
        if (session != null) {
            session.invalidate();
        }
        // 메인 페이지로 리다이렉트 한다.
        return "redirect:/";
    }
    
    @GetMapping("myPage")
    public String myPage(Model model,@SessionAttribute(value="loginMember",required = false) Member loginMember) {
    	model.addAttribute("loginMember",loginMember.getMember_id());
        return "member/mypage"  ;
    }
    
    @GetMapping("updateMember")
    public String updateMember(Model model,@SessionAttribute(value = "loginMember", required = false) Member loginMember) {
    	
        model.addAttribute("loginMember", loginMember);
        return "member/updateMember";
    }
    
    @PostMapping("updateMember")
    public ResponseEntity<String> updateMember2(Model model
    		,@RequestParam String name, @RequestParam String password, @RequestParam String phone_number
    		,@SessionAttribute(value = "loginMember", required = false) Member loginMember) {
    	
    	
    	loginMember.setPassword(password);
    	loginMember.setName(name);
    	
        memberMapper.updateMember(loginMember);
        
        return ResponseEntity.ok("변경성공");
    }
    
    @ResponseBody
    @PostMapping("idCheck")
    public Map<Object, Object> idcheck(@RequestBody String member_id) {
        int count = 0;
        
        Map<Object, Object> map = new HashMap<Object, Object>();
        System.out.println(map);
        count = memberMapper.idCheck(member_id);
        map.put("cnt", count);
 
        return map;
    }
    
    
    
	
}
