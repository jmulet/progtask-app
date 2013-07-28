<?xml version='1.0' encoding='ISO-8859-1' ?>
<!--
*     Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.
*     Use is subject to license terms.
-->
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 2.0//EN"
         "http://java.sun.com/products/javahelp/helpset_2_0.dtd">
<helpset version="2.0">


  <!-- title -->
  <title>Ajuda del programador de tasques</title>

  <!-- maps -->
  <maps>
     <homeID>org-iesapp-apps-progtask</homeID>
     <mapref location="ca/module-map.jhm" />
  </maps>

  <!-- views -->
  <view xml:lang="ca" mergetype="javax.help.UniteAppendMerge">
      <name>TOC</name>
      <label>Table Of Contents</label>
      <type>javax.help.TOCView</type>
      <data>ca/module-toc.xml</data>
  </view>
  <view xml:lang="ca" mergetype="javax.help.SortMerge">
      <name>Index</name>
      <label>Index</label>
      <type>javax.help.IndexView</type>
      <data>ca/module-index.xml</data>
  </view>
  <view xml:lang="ca">
      <name>Search</name>
      <label>Search</label>
      <type>javax.help.SearchView</type>
      <data engine="com.sun.java.help.search.DefaultSearchEngine">
          ca/JavaHelpSearch
      </data>
  </view>

</helpset>
