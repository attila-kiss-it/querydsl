package com.mysema.query.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.mysema.query.JoinExpression;
import com.mysema.query.JoinType;
import com.mysema.query.QueryMetadata;
import com.mysema.query.jpa.domain.QCat;
import com.mysema.query.jpa.domain.QCompany;
import com.mysema.query.jpa.domain.QDepartment;
import com.mysema.query.jpa.domain.QEmployee;
import com.mysema.query.jpa.domain4.QBookMark;
import com.mysema.query.jpa.domain4.QBookVersion;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.PathMetadataFactory;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.StringPath;

public class JPAQueryMixinTest {

    private JPAQueryMixin mixin = new JPAQueryMixin();

    @Test
    public void Where_Null() {
        mixin.where((Predicate)null);
    }

    @Test
    public void OrderBy() {
        QCat cat = QCat.cat;
        QCat cat_mate = new QCat("cat_mate");
        mixin.from(cat);
        mixin.orderBy(cat.mate.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                new JoinExpression(JoinType.DEFAULT, cat),
                new JoinExpression(JoinType.LEFTJOIN, cat.mate.as(cat_mate))),
                md.getJoins());
        assertEquals(Arrays.asList(cat_mate.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_NonRoot_Twice() {
        QDepartment department = QDepartment.department;
        QCompany department_company = new QCompany("department_company");
        QEmployee department_company_ceo = new QEmployee("department_company_ceo");
        mixin.from(department);
        mixin.orderBy(department.company.ceo.firstName.asc(), department.company.ceo.lastName.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                        new JoinExpression(JoinType.DEFAULT, department),
                        new JoinExpression(JoinType.LEFTJOIN, department.company.as(department_company)),
                        new JoinExpression(JoinType.LEFTJOIN, department_company.ceo.as(department_company_ceo))),
                md.getJoins());
        assertEquals(Arrays.asList(department_company_ceo.firstName.asc(), department_company_ceo.lastName.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Where() {
        QCat cat = QCat.cat;
        mixin.from(cat);
        mixin.where(cat.mate.name.isNotNull());
        mixin.orderBy(cat.mate.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(new JoinExpression(JoinType.DEFAULT, cat)), md.getJoins());
        assertEquals(Arrays.asList(cat.mate.name.asc()), md.getOrderBy());
    }

    @Test
    public void OrderBy_GroupBy() {
        QCat cat = QCat.cat;
        mixin.from(cat);
        mixin.groupBy(cat.mate.name);
        mixin.orderBy(cat.mate.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(new JoinExpression(JoinType.DEFAULT, cat)), md.getJoins());
        assertEquals(Arrays.asList(cat.mate.name.asc()), md.getOrderBy());
    }

    @Test
    public void OrderBy_Operation() {
        QCat cat = QCat.cat;
        QCat cat_mate = new QCat("cat_mate");
        mixin.from(cat);
        mixin.orderBy(cat.mate.name.lower().asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                        new JoinExpression(JoinType.DEFAULT, cat),
                        new JoinExpression(JoinType.LEFTJOIN, cat.mate.as(cat_mate))),
                md.getJoins());
        assertEquals(Arrays.asList(cat_mate.name.lower().asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Long() {
        QCat cat = QCat.cat;
        QCat catMate = new QCat(PathMetadataFactory.forProperty(cat, "mate"));
        QCat cat_mate = new QCat("cat_mate");
        QCat cat_mate_mate = new QCat("cat_mate_mate");
        mixin.from(cat);
        mixin.orderBy(cat.mate.mate.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                new JoinExpression(JoinType.DEFAULT, cat),
                new JoinExpression(JoinType.LEFTJOIN, cat.mate.as(cat_mate)),
                new JoinExpression(JoinType.LEFTJOIN, cat_mate.mate.as(cat_mate_mate))),
                md.getJoins());
        assertEquals(Arrays.asList(cat_mate_mate.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Reuse() {
        QCat cat = QCat.cat;
        QCat mate = new QCat("mate");
        mixin.from(cat);
        mixin.leftJoin(cat.mate, mate);
        mixin.orderBy(cat.mate.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                new JoinExpression(JoinType.DEFAULT, cat),
                new JoinExpression(JoinType.LEFTJOIN, cat.mate.as(mate))),
                md.getJoins());
        assertEquals(Arrays.asList(mate.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Long_Reuse() {
        QCat cat = QCat.cat;
        QCat mate = new QCat("mate");
        QCat mate_mate = new QCat("mate_mate");
        mixin.from(cat);
        mixin.leftJoin(cat.mate, mate);
        mixin.orderBy(cat.mate.mate.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                new JoinExpression(JoinType.DEFAULT, cat),
                new JoinExpression(JoinType.LEFTJOIN, cat.mate.as(mate)),
                new JoinExpression(JoinType.LEFTJOIN, mate.mate.as(mate_mate))),
                md.getJoins());
        assertEquals(Arrays.asList(mate_mate.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Any() {
        QCat cat = QCat.cat;
        QCat cat_kittens = new QCat("cat_kittens");
        mixin.from(cat);
        mixin.orderBy(cat.kittens.any().name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                new JoinExpression(JoinType.DEFAULT, cat),
                new JoinExpression(JoinType.LEFTJOIN, cat.kittens.as(cat_kittens))),
                md.getJoins());
        assertEquals(Arrays.asList(cat_kittens.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Embeddable() {
        QBookVersion bookVersion = QBookVersion.bookVersion;
        mixin.from(bookVersion);
        mixin.orderBy(bookVersion.definition.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(new JoinExpression(JoinType.DEFAULT, bookVersion)),
                md.getJoins());
        assertEquals(Arrays.asList(bookVersion.definition.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Embeddable2() {
        QArticle article = QArticle.article;
        QArticle article_content_article = new QArticle("article_content_article");
        mixin.from(article);
        mixin.orderBy(article.content.article.name.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(
                new JoinExpression(JoinType.DEFAULT, article),
                new JoinExpression(JoinType.LEFTJOIN, article.content.article.as(article_content_article))),
                md.getJoins());
        assertEquals(Arrays.asList(article_content_article.name.asc()),
                md.getOrderBy());
    }

    @Test
    public void OrderBy_Embeddable_Collection() {
        QBookVersion bookVersion = QBookVersion.bookVersion;
        QBookMark bookMark = new QBookMark("bookVersion_definition_bookMarks");
        mixin.from(bookVersion);
        mixin.orderBy(bookVersion.definition.bookMarks.any().comment.asc());

        QueryMetadata md = mixin.getMetadata();
        assertEquals(Arrays.asList(new JoinExpression(JoinType.DEFAULT, bookVersion)),
                md.getJoins());
        assertEquals(Arrays.asList(new StringPath(bookVersion.definition.bookMarks, "comment").asc()),
                md.getOrderBy());

    }

    @Test
    public void OrderBy_NullsLast() {
        QCat cat = QCat.cat;
        mixin.from(cat);
        mixin.orderBy(cat.mate.name.asc().nullsLast());
        assertEquals(
                OrderSpecifier.NullHandling.NullsLast,
                mixin.getMetadata().getOrderBy().get(0).getNullHandling());
    }
}
