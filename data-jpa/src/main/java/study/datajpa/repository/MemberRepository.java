package study.datajpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom ,JpaSpecificationExecutor<Member>{
//    List<Member> findByUsername(String username);

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    // 스프링 데이터 JPA는 메소드 이름을 분석해서 JPQL을 생성하고 실행

    @Query(name = "Member.findByUsername")  // 얘를 주석처리해도 실행이 된다.
    // 스프링 데이터 JPA는 선언한 "도메인 클래스 + .(점) + 메서드 이름"으로 Named 쿼리를 찾아서 실행
    // - 만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용
    List<Member> findByUsername(@Param("username") String username);
    // @Param : 명확하게 JPQL이 있을 경우. 현재 Member 엔티티에는 NamedQuery로 JPQL이 작성되어 있다

    @Query("select m from Member m where m.username = :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    // 단순히 값 하나를 조회
    // JPA 값 타입(@Embedded)도 이 방식으로 조회할 수 있다
    @Query("select m.username from Member m")
    List<String> findUsernameList();
    
    // DTO로 직접 조회
    // 주의 : DTO로 직접 조회하려면 JPA의 new 명령어를 사용해야 한다
    @Query("select new study.datajpa.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    // 컬렉션 파라미터 바인딩
    // Collection 타입으로 in절 지원
    @Query("select m from Member m where m.username in :names")
    List<Member> findByNames(@Param("names") Collection<String> names);
    
    // 반환 타입
    List<Member> findListByUsername(String username); // 컬렉션
    Member findMemberByUsername(String username);    // 단건
    Optional<Member> findOptionalByUsername(String username);   // 단건 Optional

    // 페이징
    @Query(value = "select m from Member m left join m.team t", countQuery = "select count(m.username) from Member m")
    // 카운트 같은 경우는 조인을 할 필요가 없다. 하지만 페이징을 하는 과정에서 조인을 할 경우, 카운트에도 조인 구문이 들어가서 성능이 떨어진다.
    // 쿼리가 단순할 경우에는 그냥 사용해도 되지만, 쿼리가 복잡해지면 카운트 쿼리를 분리하는 것이 좋다
    Page<Member> findByAge(int age, Pageable pageable);
    // Pageable : 페이징에 대한 조건(몇 개를 건너뛰고, 몇 개를 가져올지, 어떻게 정렬할지 등)

    @Modifying(clearAutomatically = true)  // JPA의 executeUpdate의 역할을 하는 어노테이션. 벌크성 수정, 삭제 하려면 붙여야한다
    // clearAutomatically : 영속성 컨텍스트 초기화를 자동으로 해준다
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    // 페치 조인
    // 1. JPQL을 사용한 페치 조인
    @Query("select m from Member m left join fetch m.team t")
    List<Member> findMemberFetchJoin();
    
    // 2. 공통 메서드 오버라이딩
    @Override
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    // 3. JPQL + 엔티티 그래프
    @EntityGraph(attributePaths = {"team"})
    @Query("select m from Member m")
    List<Member> findMemberEntityGraph();

    // 4. 메서드 이름으로 쿼리에서
    // 5. NamedEntityGraph : 엔티티에서 NamedEntityGraph를 작성해두고 사용
//    @EntityGraph(attributePaths = {"team"})
    @EntityGraph("Member.all")
    List<Member> findEnityGraphByUsername(@Param("username") String username);

    // JPA Hint
    // readOnly를 붙여두면 데이터를 호출하더라도, 영속성 컨텍스트에서 스냅샷을 준비하지 않는다 -> 변경 감지를 하지 않는다
    @QueryHints(value = @QueryHint(name = "org.hibernate.readOnly", value = "true"))
    Member findReadOnlyByUsername(String username);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
//    @Lock(LockModeType.OPTIMISTIC)
    List<Member> findLockByUsername(String username);


}

// 스프링 데이터 JPA가 제공하는 쿼리 메소드 기능
// - 조회 : find...By, read...By, query...By, get...By
//  - findHelloBy 처럼 ...에 식별하기 위한 내용(설명)이 들어가도 된다
//  - By 뒤에 아무것도 넣지 않으면 모든 데이터를 가져온다
// - COUNT : count...By 반환타입 long
// - EXISTS : exists...By 반환타입 boolean
// - 삭제 : delete...By, remove...By 반환타입 long
// - DISTINCT : findDistinct, findMemberDistinctBy
// - LIMIT : findFirst3, findFirst, findTop, findTop3

// 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 함께 변경해야 한다
// - 그렇지 않으면 애플리케이션을 시작하는 시점에 오류가 발생
// - 이렇게 애플리케이션 로딩 시점에 오류를 인지할 수 있는 것이 스프링 데이터 JPA의 매우 큰 장점

