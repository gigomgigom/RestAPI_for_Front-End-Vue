package com.mycompany.webapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mycompany.webapp.dto.Board;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/board")
public class BoardController {
	//Get방식 보내는 방법
	@GetMapping("/list")
	public List<Board> list() {
		List<Board> boardList = new ArrayList<>();
		for(int i= 1; i<=10; i++) {
			Board board = new Board();
			board.setBno(i);
			board.setBtitle("제목" + i);
			board.setBwriter("작성자" + i);
			boardList.add(board);
		}
		return boardList;
	}
	
	@GetMapping("/read/{bno}")
	public Board read(@PathVariable int bno) {
		Board board = new Board();
		board.setBno(3);
		board.setBtitle("BadBadDay");
		board.setBwriter("GGingGGing");
		return board;
	}
	
	@PostMapping("/create")
	public Board create(Board board) {
		log.info(board.toString());
		MultipartFile mf = board.getBattach();
		board.setBattachoname(mf.getOriginalFilename());
		board.setBattachtype(mf.getContentType());
		//Service를 통해서 게시물을 저장함.
		board.setBattach(null);
		return board;
	}
	
	@PutMapping("/update")
	public Board update(@RequestBody Board board) {
		log.info(board.toString());
		//요청 DTO를 통해 Service에서 게시물 수정
		return board;
	}
	
}
