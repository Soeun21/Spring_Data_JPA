package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    // JPAQueryFactory를 필드로
    JPAQueryFactory queryFactory;
    // JPAQueryFactory를 필드로 제공하면 동시성 문제
    //  - 동시성 문제는 JPAQueryFactory를 생성할 때 제공하는 EntityManger(em)에 달려있다.
    //  - 스프링 프레임워크는 여러 쓰레드에서 동시에 같은 EntityManger에 접근해도, 트랜잭션 마다 별도의 영속성 컨텍스트를 제공하기 때문에, 동시성 문제는 걱정하지 않아도 된다

    @BeforeEach // 각 테스트 실행 전 데이터 세팅
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }
//    JPQL : 문자(실행 시점 오류), QueryDSL : 코드(컴파일 시점 오류)
//    JPQL : 파라미터 바인딩 직접, QueryDSL : 파라미터 바인딩 자동 처리
    @Test
    public void startJPQL() throws Exception {
        // member1을 찾아라
        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class).setParameter("username", "member1").getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // JPAQueryFactory를 필드로 빼기 전
    @Test
    public void startQuerydsl() throws Exception {
        // member1을 찾아라
        JPAQueryFactory queryFactory = new JPAQueryFactory(em); // EntityManger로 JPAQueryFactory 생성,
        QMember m = new QMember("m");   // 변수에다가 별칭을 줘야 한다

        Member findMember = queryFactory
                            .selectFrom(m)
                            .from(m)
                            .where(m.username.eq("member1"))    // 파라미터 바인딩 처리
                            .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    // JPAQueryFactory를 필드로 뺀 후
    @Test
    public void startQuerydsl2() throws Exception {
        // member1을 찾아라
        QMember m = new QMember("m");   // 변수에다가 별칭을 줘야 한다

        Member findMember = queryFactory
                .selectFrom(m)
                .from(m)
                .where(m.username.eq("member1"))    // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    // Q클래스 사용 기본
    @Test
    public void startQuerydsl3() throws Exception {
        // Q클래스 인스턴스를 사용하는 2가지 방법
        // 별칭 직접 지정, 같은 테이블을 조인해야 하는 경우에만 사용
//        QMember qMember = new QMember("m");
        // 기본 인스턴스 사용
//        QMember qMember = QMember.member;

        // 기본 인스턴스를 static import와 함께 사용
        Member findMember = queryFactory.selectFrom(member).from(member).where(member.username.eq("member1")).fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 검색 조건 쿼리
    @Test
    public void search() throws Exception {
        Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1").and(member.age.eq(10))).fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    // JPQL이 제공하는 모든 검색 조건 제공
        /*
        member.username.eq("member1") // username = 'member1'
        member.username.ne("member1") //username != 'member1'
        member.username.eq("member1").not() // username != 'member1'

        member.username.isNotNull() //이름이 is not null

        member.age.in(10, 20) // age in (10,20)
        member.age.notIn(10, 20) // age not in (10, 20)
        member.age.between(10,30) //between 10, 30

        member.age.goe(30) // age >= 30
        member.age.gt(30) // age > 30
        member.age.loe(30) // age <= 30
        member.age.lt(30) // age < 30

        member.username.like("member%") //like 검색
        member.username.contains("member") // like ‘%member%’ 검색
        member.username.startsWith("member") //like ‘member%’ 검색
        */
    }
    
    // AND 조건을 파라미터로 처리
    @Test
    public void searchAndParam() throws Exception {
        Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1"), member.age.eq(10)).fetchOne();
        // where()에 파라미터로 검색 조건을 추가하면 AND 조건이 추가됨
        // 이 경우 null 값은 무시 -> 메서드 추출을 활용해서 동적 쿼리를 깔끔하게 만들 수 있다
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 결과 조회
    // fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
    // fetchOne() : 단 건 조회
    //  - 결과가 없으면 null
    //  - 결과가 둘 이상이면 com.querydsl.core.NonUniqueResultException
    // fetchFirst() : limit(1).fetchOne()
    // fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
    // fetchCount() : count 쿼리로 변경 해서 count 수 조회
    @Test
    public void resultFetch() throws Exception {
        // List
        List<Member> fetch = queryFactory.selectFrom(member).fetch();

        // 단 건
        Member fetchOne = queryFactory.selectFrom(member).fetchOne();

        // 처음 한 건 조회
        Member fetchFirst = queryFactory.selectFrom(member).fetchFirst();
        
        // 페이징에서 사용
        QueryResults<Member> results = queryFactory.selectFrom(member).fetchResults();
        results.getTotal();
        List<Member> content = results.getResults();

        // count 쿼리로 변경
        long count = queryFactory.selectFrom(member).fetchCount();
    }
    
    // 정렬
    /*
        회원 정렬 순서
        1. 회원 나이 내림차순(desc)
        2. 회원 이름 올림차순(asc(
            단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() throws Exception {
        // given
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        // when
        List<Member> result = queryFactory.selectFrom(member).where(member.age.eq(100)).orderBy(member.age.desc(), member.username.asc().nullsLast()).fetch();

        // then
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }
    
    // 페이징
    @Test
    public void paging1() throws Exception {
        List<Member> result = queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2).fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    // 전체 조회수가 필요하면
    @Test
    public void paging2() throws Exception {
        QueryResults<Member> results = queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2).fetchResults();

        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
        assertThat(results.getResults().size()).isEqualTo(2);
        
        // 주의 : count 쿼리가 실행되니 성능상 주의
        // 참고 : 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
        //  count 쿼리는 조인이 필요 없는 경우도 있다
        //  - 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두 조인을 해버리기 때문에 성능이 안나올 수 있다
        //  - count 쿼리에 조인이 필요없는 성능 최적화가 필요하면, count 전용 쿼리를 별도로 작성해야 한다
    }

    // 집합 함수
    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이
     * from Member m
     */
    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }
    
    // GroupBy 사용
    // 팀의 이름과 각 팀의 평균 연령을 구해라
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    // 조인 - 기본 조인
    // 조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고, 두 번째 파라미터에 별칭(alias)으로 사용할 Q 타입을 지정하면 된다
    // join(조인 대상, 별칭으로 사용할 Q타입)
    @Test
    public void join() throws Exception {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        // join(), innerJoin() : 내부 조인(inner join)
        // leftJoin() : left 외부 조인(left outer join)
        // rightJoin() : right 외부 조인(right outer join)
        // JPQL의 on과 성능 최적화를 위한 fetch 조인 제공
    }
    
    // 세타 조인 : 연관 관계가 없는 필드로 조인
    // 회원의 이름이 팀 이름과 같은 회원 조회
    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        // from 절에 여러 엔티티를 선택해서 세타 조인
        // 외부 조인 불가능
    }

    // 조인 - on절
    // ON절을 활용한 조인(JPA 2.1부터 지원)
    //  - 조인 대상 필터링
    //  - 연관관계 없는 엔티티 외부 조인

    // 1. 조인 대상 필터링
    // ex) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
    // JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
    @Test
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
//                .leftJoin(member.team, team).on(team.name.eq("teamA"))
//                .leftJoin(member.team, team).on(team.name.eq("teamA")).where(team.name.eq("teamA"))
                .join(member.team, team).where(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        
        // 참고
        // on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인을 사용하면, where 절에서 필터링 하는 것과 기능이 동일하다
        //  - 따라서 on 절을 활용한 조인 대상 필터링을 사용할 때, 내부조인이면 익숙한 where절로 해결하고, 정말 외부조인이 필요한 경우에만 이 기능을 사용하자
    }

    // 2. 연관관계 없는 엔티티 외부 조인
    // ex) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
    // JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        // 하이버네이트 5.1부터 on을 사용해서 서로 관계가 없는 필드로 외부 조인하는 기능이 추가되었다
        //  - 물론 내부 조인도 가능하다
        // 주의! 문법을 잘 봐야 한다.
        //  - leftJoin() 부분에 일반 조인과 다르게 엔티티 하나만 들어간다
        //      - 일반 조인 : leftJoin(member.team, team)
        //      - on조인 : from(member).leftJoin(team).on(xxx)
    }
}
