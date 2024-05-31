package com.mycompany.webapp.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;
import com.mycompany.webapp.dto.Pager;
import com.mycompany.webapp.service.BoardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
public class BoardController {
	
	@Autowired
	private BoardService boardService;
	
	//Get방식 보내는 방법
	@GetMapping("/list")
	public Map<String, Object> list(@RequestParam(defaultValue="1") int pageNo) {
		//페이징 대상이 되는 전체 행수 얻기
		int totalRows = boardService.getCount();
		//페이저 객체 생성
		Pager pager = new Pager(10, 5, totalRows, pageNo);
		//해당 페이지의 게시물 목록 가져오기
		List<Board> boardList = boardService.getList(pager);
		//여러 객체를 리턴하기 위해 Map객체 생성 후 객체들을 추가한다.
		Map<String, Object> map = new HashMap<>();
		map.put("boards", boardList);
		map.put("pager", pager);
		
		return map;
	}
	
	@PostMapping("/create")
	public Board create(Board board) {
		//받아온 데이터중에서 첨부파일 존재 여부에 따라 DB에 파일 추가 여부를 결정한다.
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			//받아온 
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		board.setBwriter("user");
		
		boardService.insert(board);
		
		//JSON으로 변환되지 않거나 변환할 필요없는 필드는 null 처리한다.
		board.setBattach(null);
		board.setBattachdata(null);
		return board;
	}
	
	@GetMapping("/read/{bno}")
	public Board read(@PathVariable int bno) {
		Board board = boardService.getBoard(bno);
		board.setBattachdata(null);
		return board;
	}
	
	@PutMapping("/update")
	public Board update(Board board) {
		if(board.getBattach() != null && !board.getBattach().isEmpty()) {
			//받아온 
			MultipartFile mf = board.getBattach();
			board.setBattachoname(mf.getOriginalFilename());
			board.setBattachtype(mf.getContentType());
			try {
				board.setBattachdata(mf.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		boardService.update(board);
		
		board = boardService.getBoard(board.getBno());
		//JSON으로 변환되지 않는 필드는 null 처리
		board.setBattachdata(null);
		
		return board;
	}
	
	@DeleteMapping("/delete/{bno}")
	public void delete(@PathVariable int bno) {
		boardService.delete(bno);
	}
	
	@GetMapping("/battach/{bno}")
	public void battach(@PathVariable int bno, HttpServletResponse response) {
		//해당 게시물 가져오기
		Board board = boardService.getBoard(bno);
		//파일 이름이 한글이 ㄹ경우, 브라우저에서 한글 이름으로 다운로드 받기 위한 코드
		try {
			String fileName = new String(board.getBattachoname().getBytes("UTF-8"), "ISO-8859-1");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		response.setContentType(board.getBattachtype());
		//응답 바디에 파일 데이터를 출력
		OutputStream os;
		try {
			os = response.getOutputStream();
			os.write(board.getBattachdata());
			os.flush();
			os.close();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
