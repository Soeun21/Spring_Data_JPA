package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Test
    public void testMember() throws Exception {
        // given
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        // when
        Member findMember = memberRepository.findById(savedMember.getId()).get();

        // then
        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);

    }

    @Test
    public void findByUsernameAndAgeGreaterThan() throws Exception {
        // given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        // then
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void testNamedQuery() throws Exception {
        // given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findByUsername("AAA");

        // then
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void testQuery() throws Exception {
        // given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findUser("AAA", 10);

        // then
        assertThat(result.get(0)).isEqualTo(m1);
    }

    @Test
    public void findUsernameList() throws Exception {
        // given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<String> result = memberRepository.findUsernameList();

        // then
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void findMemberDto() throws Exception {
        // given
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        // when
        List<MemberDto> memberDto = memberRepository.findMemberDto();

        // then
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames() throws Exception {
        // given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));

        // then
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType() throws Exception {
        // given
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        // when
        List<Member> aaa = memberRepository.findListByUsername("AAA");

        Member bbb = memberRepository.findMemberByUsername("AAA");
        System.out.println("bbb = " + bbb);

        Optional<Member> ccc = memberRepository.findOptionalByUsername("AAA");

        List<Member> result = memberRepository.findListByUsername("ADFAFDAFA");
        System.out.println("result = " + result);
        // 반환값이 컬렉션일 때, 결과가 없을 때는 빈 컬렉션을 반환받기 때문에 null을 신경쓰지 않아도 된다

        Member findMember = memberRepository.findMemberByUsername("ADFADFAF");
        System.out.println("findMember = " + findMember);
        // 단건일 경우, 결과가 없으면 null이 반환된다

        Optional<Member> findOptional = memberRepository.findOptionalByUsername("ADFAFDAFA");
        System.out.println("findOptional = " + findOptional);
        // 단건을 조회할 경우에는 객체를 바로 받는 것 보다는 Optional을 사용하자
        // 조회된 데이터가 2건 이상일 경우 예외가 발생한다

    }

    @Test
    public void paging() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

        // when
        Page<Member> page = memberRepository.findByAge(age, pageRequest);   // Page 외에도 Slice와 List로 받을 수 있다
        // Page를 반환 값으로 잡으면 totalCount 쿼리가 자동으로 실행된다
        // Pageable은 인터페이스다. 따라서 실제 사용할 때는 해당 인터페이스를 구현한 PageRequest 객체를 사용한다
        // PageRequest 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를 입력한다
        //  - 추가로 정렬 정보도 파라미터로 사용할 수 있다.
        // 주의 : Page는 1부터 시작이 아니라 0부터 시작이다

        // 엔티티를 애플리케이션 바깥으로 노출시키면 안된다
        // Page<Member>를 dto로 만들기
        Page<MemberDto> dtoPage = page.map(m -> new MemberDto(m.getId(), m.getUsername(), m.getTeam().getName()));

        // then
        List<Member> content = page.getContent();   // 조회된 데이터
        long totalElements = page.getTotalElements();   // totalCount

        for (Member member : content) {
            System.out.println("member = " + member);
        }
        System.out.println("totalElements = " + totalElements);

        assertThat(content.size()).isEqualTo(3);    // 조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5);   // 전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0);  // 페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2);  // 전체 페이지 번호
        assertThat(page.isFirst()).isTrue();    // 첫 번째 항목인가?
        assertThat(page.hasNext()).isTrue();    // 다음 페이지가 있는가?
    }

    @Test
    public void bulkUpdate() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        // when
        int resultCount = memberRepository.bulkAgePlus(20);

        List<Member> result = memberRepository.findByUsername("member5");
        Member member1 = result.get(0);
        System.out.println("member1 = " + member1.getAge());
        
        // 벌크성 쿼리 주의점
        // - 영속성 컨텍스트를 무시하고 바로 DB에 전달하기 때문에, 벌크성 쿼리를 사용 후 영속성 컨텍스트를 초기화 해야 한다
//        em.flush();
//        em.clear();

        List<Member> result2 = memberRepository.findByUsername("member5");
        Member member2 = result2.get(0);
        System.out.println("member2 = " + member2.getAge());

        // then
        assertThat(resultCount).isEqualTo(3);
    }
    
    @Test
    public void findMemberLazy() throws Exception {
        // given
        // member1 -> teamA
        // member2 -> teamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        // when
        // select Member
//        List<Member> members = memberRepository.findMemberFetchJoin();
//        List<Member> members = memberRepository.findAll();  // MemberRepository에서 오버라이딩
//        List<Member> members = memberRepository.findMemberEntityGraph();
        List<Member> members = memberRepository.findEnityGraphByUsername("member1");

        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.getTeam().getClass() = " + member.getTeam().getClass());
            System.out.println("member.getTeam().getName() = " + member.getTeam().getName());
        }

        // then
    }

    @Test
    public void queryHint() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        member.setUsername("member2");
        em.flush();

        // then

    }

    @Test
    public void lock() throws Exception {
        // given
        memberRepository.save(new Member("member1", 10));
        em.flush();
        em.clear();

        // when
        List<Member> member = memberRepository.findLockByUsername("member1");

        // then

    }
}
