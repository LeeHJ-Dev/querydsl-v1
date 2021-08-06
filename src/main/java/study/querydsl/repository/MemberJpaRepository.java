package study.querydsl.repository;

import com.google.common.annotations.VisibleForTesting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;


@Repository
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public MemberJpaRepository(EntityManager entityManager){
        this.em = entityManager;
        jpaQueryFactory = new JPAQueryFactory(this.em);
    }

    public void save(Member member){
        em.persist(member);
    }

    public Optional<Member> findById(Long id){
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }




    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    public List<Member> findAll_Querydsl(){
        return jpaQueryFactory
                .select(member)
                .from(member)
                .fetch();
    }



    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m where m.username =:username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
    public List<Member> findByUsername_Querydsl(String username){
        return jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }

        if (StringUtils.hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }

        if (condition.getAgeGoe() != null){
            builder.and(member.age.goe(condition.getAgeGoe()));
        }

        if (condition.getAgeLoe() != null){
            builder.and(member.age.goe(condition.getAgeLoe()));
        }

        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id       .as("memberId"),
                        member.username .as("username"),
                        member.age      .as("age"),
                        team.id         .as("teamId"),
                        team.name       .as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();

    }
}
