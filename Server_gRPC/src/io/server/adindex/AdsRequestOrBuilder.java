// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: SelectAds.proto

package io.server.adindex;

public interface AdsRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:adindex.AdsRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .adindex.Query query = 1;</code>
   */
  java.util.List<io.server.adindex.Query> 
      getQueryList();
  /**
   * <code>repeated .adindex.Query query = 1;</code>
   */
  io.server.adindex.Query getQuery(int index);
  /**
   * <code>repeated .adindex.Query query = 1;</code>
   */
  int getQueryCount();
  /**
   * <code>repeated .adindex.Query query = 1;</code>
   */
  java.util.List<? extends io.server.adindex.QueryOrBuilder> 
      getQueryOrBuilderList();
  /**
   * <code>repeated .adindex.Query query = 1;</code>
   */
  io.server.adindex.QueryOrBuilder getQueryOrBuilder(
          int index);
}