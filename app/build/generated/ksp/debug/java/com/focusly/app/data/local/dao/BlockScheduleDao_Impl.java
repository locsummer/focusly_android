package com.focusly.app.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.focusly.app.data.local.entity.BlockScheduleEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BlockScheduleDao_Impl implements BlockScheduleDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BlockScheduleEntity> __insertionAdapterOfBlockScheduleEntity;

  private final EntityDeletionOrUpdateAdapter<BlockScheduleEntity> __deletionAdapterOfBlockScheduleEntity;

  private final EntityDeletionOrUpdateAdapter<BlockScheduleEntity> __updateAdapterOfBlockScheduleEntity;

  public BlockScheduleDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBlockScheduleEntity = new EntityInsertionAdapter<BlockScheduleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `block_schedules` (`id`,`title`,`startHour`,`startMinute`,`endHour`,`endMinute`,`daysOfWeek`,`blockedPackages`,`isEnabled`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockScheduleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getStartHour());
        statement.bindLong(4, entity.getStartMinute());
        statement.bindLong(5, entity.getEndHour());
        statement.bindLong(6, entity.getEndMinute());
        statement.bindString(7, entity.getDaysOfWeek());
        statement.bindString(8, entity.getBlockedPackages());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__deletionAdapterOfBlockScheduleEntity = new EntityDeletionOrUpdateAdapter<BlockScheduleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `block_schedules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockScheduleEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBlockScheduleEntity = new EntityDeletionOrUpdateAdapter<BlockScheduleEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `block_schedules` SET `id` = ?,`title` = ?,`startHour` = ?,`startMinute` = ?,`endHour` = ?,`endMinute` = ?,`daysOfWeek` = ?,`blockedPackages` = ?,`isEnabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockScheduleEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindLong(3, entity.getStartHour());
        statement.bindLong(4, entity.getStartMinute());
        statement.bindLong(5, entity.getEndHour());
        statement.bindLong(6, entity.getEndMinute());
        statement.bindString(7, entity.getDaysOfWeek());
        statement.bindString(8, entity.getBlockedPackages());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final BlockScheduleEntity schedule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBlockScheduleEntity.insertAndReturnId(schedule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final BlockScheduleEntity schedule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBlockScheduleEntity.handle(schedule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final BlockScheduleEntity schedule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBlockScheduleEntity.handle(schedule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BlockScheduleEntity>> getAllSchedules() {
    final String _sql = "SELECT * FROM block_schedules";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"block_schedules"}, new Callable<List<BlockScheduleEntity>>() {
      @Override
      @NonNull
      public List<BlockScheduleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfDaysOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeek");
          final int _cursorIndexOfBlockedPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedPackages");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<BlockScheduleEntity> _result = new ArrayList<BlockScheduleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockScheduleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final String _tmpDaysOfWeek;
            _tmpDaysOfWeek = _cursor.getString(_cursorIndexOfDaysOfWeek);
            final String _tmpBlockedPackages;
            _tmpBlockedPackages = _cursor.getString(_cursorIndexOfBlockedPackages);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new BlockScheduleEntity(_tmpId,_tmpTitle,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpDaysOfWeek,_tmpBlockedPackages,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getEnabledSchedules(
      final Continuation<? super List<BlockScheduleEntity>> $completion) {
    final String _sql = "SELECT * FROM block_schedules WHERE isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<BlockScheduleEntity>>() {
      @Override
      @NonNull
      public List<BlockScheduleEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfStartHour = CursorUtil.getColumnIndexOrThrow(_cursor, "startHour");
          final int _cursorIndexOfStartMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "startMinute");
          final int _cursorIndexOfEndHour = CursorUtil.getColumnIndexOrThrow(_cursor, "endHour");
          final int _cursorIndexOfEndMinute = CursorUtil.getColumnIndexOrThrow(_cursor, "endMinute");
          final int _cursorIndexOfDaysOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "daysOfWeek");
          final int _cursorIndexOfBlockedPackages = CursorUtil.getColumnIndexOrThrow(_cursor, "blockedPackages");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<BlockScheduleEntity> _result = new ArrayList<BlockScheduleEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockScheduleEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final int _tmpStartHour;
            _tmpStartHour = _cursor.getInt(_cursorIndexOfStartHour);
            final int _tmpStartMinute;
            _tmpStartMinute = _cursor.getInt(_cursorIndexOfStartMinute);
            final int _tmpEndHour;
            _tmpEndHour = _cursor.getInt(_cursorIndexOfEndHour);
            final int _tmpEndMinute;
            _tmpEndMinute = _cursor.getInt(_cursorIndexOfEndMinute);
            final String _tmpDaysOfWeek;
            _tmpDaysOfWeek = _cursor.getString(_cursorIndexOfDaysOfWeek);
            final String _tmpBlockedPackages;
            _tmpBlockedPackages = _cursor.getString(_cursorIndexOfBlockedPackages);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new BlockScheduleEntity(_tmpId,_tmpTitle,_tmpStartHour,_tmpStartMinute,_tmpEndHour,_tmpEndMinute,_tmpDaysOfWeek,_tmpBlockedPackages,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
