package study.datajpa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id")Long id){
        Member member = memberRepository.findById(id).get();
        return member.getUsername();
    }

    @GetMapping("/members2/{id}")
    public String findMember2(@PathVariable("id")Member member){
        return member.getUsername();
    }


/*    @GetMapping("/members") // 엔티티 노출상황
    public Page<Member> list(Pageable pageable){
        Page<Member> page = memberRepository.findAll(pageable);
        return page;
    }*/

    @GetMapping("/members") // 엔티티를 dto로 감싸서 반환
    public Page<MemberDto> list(@PageableDefault(size = 5) Pageable pageable){
        Page<Member> page = memberRepository.findAll(pageable);
//        return page.map(member -> new MemberDto(member));
        return page.map(MemberDto::new);// 위와 같은 메소드
    }



    @PostConstruct
    public void init(){
        for(int i = 0; i<100; i ++){
            memberRepository.save(new Member("user" + i,i));
        }

    }




}
