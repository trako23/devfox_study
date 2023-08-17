package com.devfox.board.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.devfox.board.config.UserInfo;
import com.devfox.board.model.board.Board;
import com.devfox.board.model.board.BoardUpdateForm;
import com.devfox.board.model.board.BoardWriteForm;
import com.devfox.board.model.member.Member;
import com.devfox.board.service.BoardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("board")
@Controller
public class BoardController {
	
	private final BoardService boardService;
	
	// 후기 글쓰기 페이지 이동
    @GetMapping("write")
    public String writeForm(Model model,RedirectAttributes redirectAttributes) {
        // writeForm.html의 필드 표시를 위해 빈 BoardWriteForm 객체를 생성하여 model 에 저장한다.
        model.addAttribute("writeForm", new BoardWriteForm());
        return "board/write";
    }
  

    // 게시글 쓰기
    @PostMapping("write")
    public String write(@AuthenticationPrincipal UserInfo userInfo,
                        @Validated @ModelAttribute("writeForm") BoardWriteForm boardWriteForm,
                        BindingResult result,Model model,RedirectAttributes redirectAttributes) {
       
    	if(userInfo == null) {
    		return "redirect:/";
    	}
        
        // title과 board_place에 대한 공백 검사
        if (boardWriteForm.getTitle()==null) {
            // validation 에러 메시지를 BindingResult에 추가합니다.
            result.rejectValue("title", "NotEmpty");
            result.rejectValue("board_place", "NotEmpty");
            return "board/write"; }
      
        // 파라미터로 받은 BoardWriteForm 객체를 Board 타입으로 변환한다.
        Board board = BoardWriteForm.toBoard(boardWriteForm);
        // board 객체에 로그인한 사용자의 아이디를 추가한다.
        board.setMember_id(userInfo.getUsername());
        // board 객체를 저장한다.
        boardService.saveboard(board);
   
        // board/list 로 리다이렉트한다.
        return "redirect:/";
    }

   
    // 게시글 읽기
    @GetMapping("read")
    public String read(@RequestParam Long board_id,
                       Model model,
                       @AuthenticationPrincipal UserInfo userInfo
                       ) {
    	
        // board_id 에 해당하는 게시글을 데이터베이스에서 찾는다.
        Board board = boardService.readboard(board_id);
        // board_id에 해당하는 게시글이 없으면 리스트로 리다이렉트 시킨다.
        if (board == null) {
            log.info("게시글 없음");
            return "redirect:/board/list";
        }        
        // 모델에 Board 객체를 저장한다.
        model.addAttribute("board", board);
        model.addAttribute("loginMember",userInfo);
        return "board/read";
    }

    // 게시글 수정 페이지 이동
    @GetMapping("update")
    public String updateForm(@AuthenticationPrincipal UserInfo userInfo,
                             @RequestParam Long board_id,
                             Model model) {
        log.info("board_id: {}", board_id);

        // board_id에 해당하는 게시글이 없거나 게시글의 작성자가 로그인한 사용자의 아이디와 다르면 수정하지 않고 리스트로 리다이렉트 시킨다.
        Board board = boardService.findboard(board_id);
        if (board_id == null || !board.getMember_id().equals(userInfo.getUsername())) {
            log.info("수정 권한 없음");
            return "redirect:/board/list";
        }
        // model 에 board 객체를 저장한다.
        model.addAttribute("board", Board.toBoardUpdateForm(board));

        // board/update.html 를 찾아서 리턴한다.
        return "board/update";
    }

    // 게시글 수정
    @PostMapping("update")
    public String update(@AuthenticationPrincipal UserInfo userInfo,
                         @RequestParam Long board_id,
                         @Validated @ModelAttribute("board") BoardUpdateForm updateboard,
                         BindingResult result) {
                         
        // validation 에 에러가 있으면 board/update.html 페이지로 돌아간다.
        if (result.hasErrors()) {
            return "board/update";
        }

        // board_id 에 해당하는 Board 정보를 데이터베이스에서 가져온다.
        Board board = boardService.findboard(board_id);
        // Board 객체가 없거나 작성자가 로그인한 사용자의 아이디와 다르면 수정하지 않고 리스트로 리다이렉트 시킨다.
        if (board == null || !board.getMember_id().equals(userInfo.getUsername())) {
            log.info("수정 권한 없음");
            return "redirect:/";
        }
        // 제목을 수정한다.
        board.setTitle(updateboard.getTitle());
        // 내용을 수정한다.
        board.setContents(updateboard.getContents());
        
        // 수정한 Board 를 데이터베이스에 update 한다.
        boardService.updateboard(board);
        return "redirect:/";
    }

    // 게시글 삭제
    @GetMapping("delete")
    public String remove(@AuthenticationPrincipal UserInfo userInfo,
                         @RequestParam Long board_id) {
        // board_id 에 해당하는 게시글을 가져온다.
        Board board = boardService.findboard(board_id);
        // 게시글이 존재하지 않거나 작성자와 로그인 사용자의 아이디가 다르면 리스트로 리다이렉트 한다.
        if (board == null || !board.getMember_id().equals(userInfo.getUsername())) {
            log.info("삭제 권한 없음");
            return "redirect:/";
        }
        // 게시글을 삭제한다.
    	log.info("board_id:{}",board_id);

        boardService.removeboard(board_id);
        // board/list 로 리다이렉트 한다.
        return "redirect:/";
    }
    

   
    @GetMapping("myboardList")
    public String myBoardList(
    					@AuthenticationPrincipal UserInfo userInfo
                        ,Model model) {
    	
    	List<Board> boards = boardService.findBoardsByMemberId(userInfo.getUsername());
    	if(userInfo !=null) {
    		model.addAttribute("boards", boards);
    		model.addAttribute("loginMember",userInfo);
    	}

    	return "member/myboardList";
    }
    
    

	
}