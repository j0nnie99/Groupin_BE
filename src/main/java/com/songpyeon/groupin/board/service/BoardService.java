package com.songpyeon.groupin.board.service;

import com.songpyeon.groupin.board.domain.Board;
import com.songpyeon.groupin.board.dto.BoardWriteDto;
import com.songpyeon.groupin.board.repository.BoardRepository;
import com.songpyeon.groupin.config.auth.PrincipalDetails;
import com.songpyeon.groupin.handler.ex.CustomException;
import com.songpyeon.groupin.handler.ex.ErrorCode;
import com.songpyeon.groupin.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    @Value("${file.path}")  //application.yml 파일의 file - path의 값 가져오기
    private String uploadFolder;


    // 카테고리별 글 리스트 불러오기
    public List<Board> listByCategory(@PathVariable String category) {
        // SELECT * FROM Board where category = :category;
        List<Board> boardEntity = boardRepository.findByCategory(category);
        return boardEntity;
    }


    // 글 작성하기
    public void save(@PathVariable String category, BoardWriteDto boardWriteDto, PrincipalDetails principalDetails){

        UUID uuid = UUID.randomUUID();  //UUID: 파일명과 별개로 고유한 아이디를 만들기 위해 사용하는 규약
        String imageFileName = uuid+"_"+boardWriteDto.getImage_file().getOriginalFilename(); // 업로드하려는 실제 파일명이 들어가는 부분
        Path imageFilePath = Paths.get(uploadFolder+imageFileName);

        if (boardWriteDto.getImage_file().isEmpty()){
            System.out.println("사진이 업로드 되지 않았습니다.");
        }
        else{
        // 통신, I/O -> 예외가 발생할 수 있음
            try {
                Files.write(imageFilePath, boardWriteDto.getImage_file().getBytes());
                System.out.println(imageFileName + " 업로드 성공!");
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        // DB에 저장하기
        // BoardWriteDto에 있는 정보를 Board로 넘겨서 저장하는 과정이 필요
        // Dto에서 toEntity 함수 만들어서 가능
        boardWriteDto.setCategory(category);    // pathvariable로 받아온 category 정보를 Dto에 set
        boardWriteDto.setUser(principalDetails.getUser());
        Board board = boardWriteDto.toEntity(imageFileName);    //principalDetails.getUser()로 imageFileName처럼 유저 정보 받아야 함
        Board boardEntity = boardRepository.save(board);
    }

    // 글 상세보기
    public Board detail(@PathVariable String category, @PathVariable int id){
        Board boardEntity = boardRepository.findByCategoryAndId(category, id);
        if (boardEntity == null){
            throw new CustomException(ErrorCode.POSTS_NOT_FOUND);
        }
        return boardEntity;
    }

}