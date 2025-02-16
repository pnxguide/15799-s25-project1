package edu.cmu.cs.db.calcite_app.app.rules;

import com.google.common.base.MoreObjects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Var;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import org.apache.calcite.plan.RelRule;
import org.apache.calcite.tools.RelBuilderFactory;
import org.immutables.value.Generated;

/**
 * {@code ImmutableFilterDistributiveRule} contains immutable implementation classes generated from
 * abstract value types defined as nested inside {@link FilterDistributiveRule}.
 * @see ImmutableFilterDistributiveRule.Config
 */
@Generated(from = "FilterDistributiveRule", generator = "Immutables")
@SuppressWarnings({"all"})
@ParametersAreNonnullByDefault
@javax.annotation.processing.Generated("org.immutables.processor.ProxyProcessor")
public final class ImmutableFilterDistributiveRule {
  private ImmutableFilterDistributiveRule() {}

  /**
   * Immutable implementation of {@link FilterDistributiveRule.Config}.
   * <p>
   * Use the builder to create immutable instances:
   * {@code ImmutableFilterDistributiveRule.Config.builder()}.
   */
  @Generated(from = "FilterDistributiveRule.Config", generator = "Immutables")
  @Immutable
  @CheckReturnValue
  public static final class Config implements FilterDistributiveRule.Config {
    private final @Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String description;
    private final RelRule.OperandTransform operandSupplier;
    private final RelBuilderFactory relBuilderFactory;

    private Config(ImmutableFilterDistributiveRule.Config.Builder builder) {
      this.description = builder.description;
      if (builder.operandSupplier != null) {
        initShim.operandSupplier(builder.operandSupplier);
      }
      if (builder.relBuilderFactory != null) {
        initShim.relBuilderFactory(builder.relBuilderFactory);
      }
      this.operandSupplier = initShim.operandSupplier();
      this.relBuilderFactory = initShim.relBuilderFactory();
      this.initShim = null;
    }

    private Config(
        @Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String description,
        RelRule.OperandTransform operandSupplier,
        RelBuilderFactory relBuilderFactory) {
      this.description = description;
      this.operandSupplier = operandSupplier;
      this.relBuilderFactory = relBuilderFactory;
      this.initShim = null;
    }

    private static final byte STAGE_INITIALIZING = -1;
    private static final byte STAGE_UNINITIALIZED = 0;
    private static final byte STAGE_INITIALIZED = 1;
    @SuppressWarnings("Immutable")
    private transient volatile InitShim initShim = new InitShim();

    @Generated(from = "FilterDistributiveRule.Config", generator = "Immutables")
    private final class InitShim {
      private byte operandSupplierBuildStage = STAGE_UNINITIALIZED;
      private RelRule.OperandTransform operandSupplier;

      RelRule.OperandTransform operandSupplier() {
        if (operandSupplierBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
        if (operandSupplierBuildStage == STAGE_UNINITIALIZED) {
          operandSupplierBuildStage = STAGE_INITIALIZING;
          this.operandSupplier = Objects.requireNonNull(operandSupplierInitialize(), "operandSupplier");
          operandSupplierBuildStage = STAGE_INITIALIZED;
        }
        return this.operandSupplier;
      }

      void operandSupplier(RelRule.OperandTransform operandSupplier) {
        this.operandSupplier = operandSupplier;
        operandSupplierBuildStage = STAGE_INITIALIZED;
      }

      private byte relBuilderFactoryBuildStage = STAGE_UNINITIALIZED;
      private RelBuilderFactory relBuilderFactory;

      RelBuilderFactory relBuilderFactory() {
        if (relBuilderFactoryBuildStage == STAGE_INITIALIZING) throw new IllegalStateException(formatInitCycleMessage());
        if (relBuilderFactoryBuildStage == STAGE_UNINITIALIZED) {
          relBuilderFactoryBuildStage = STAGE_INITIALIZING;
          this.relBuilderFactory = Objects.requireNonNull(relBuilderFactoryInitialize(), "relBuilderFactory");
          relBuilderFactoryBuildStage = STAGE_INITIALIZED;
        }
        return this.relBuilderFactory;
      }

      void relBuilderFactory(RelBuilderFactory relBuilderFactory) {
        this.relBuilderFactory = relBuilderFactory;
        relBuilderFactoryBuildStage = STAGE_INITIALIZED;
      }

      private String formatInitCycleMessage() {
        List<String> attributes = new ArrayList<>();
        if (operandSupplierBuildStage == STAGE_INITIALIZING) attributes.add("operandSupplier");
        if (relBuilderFactoryBuildStage == STAGE_INITIALIZING) attributes.add("relBuilderFactory");
        return "Cannot build Config, attribute initializers form cycle " + attributes;
      }
    }

    private RelRule.OperandTransform operandSupplierInitialize() {
      return FilterDistributiveRule.Config.super.operandSupplier();
    }

    private RelBuilderFactory relBuilderFactoryInitialize() {
      return FilterDistributiveRule.Config.super.relBuilderFactory();
    }

    /**
     * @return The value of the {@code description} attribute
     */
    @Override
    public @Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String description() {
      return description;
    }

    /**
     * @return The value of the {@code operandSupplier} attribute
     */
    @Override
    public RelRule.OperandTransform operandSupplier() {
      InitShim shim = this.initShim;
      return shim != null
          ? shim.operandSupplier()
          : this.operandSupplier;
    }

    /**
     * @return The value of the {@code relBuilderFactory} attribute
     */
    @Override
    public RelBuilderFactory relBuilderFactory() {
      InitShim shim = this.initShim;
      return shim != null
          ? shim.relBuilderFactory()
          : this.relBuilderFactory;
    }

    /**
     * Copy the current immutable object by setting a value for the {@link FilterDistributiveRule.Config#description() description} attribute.
     * An equals check used to prevent copying of the same value by returning {@code this}.
     * @param value A new value for description (can be {@code null})
     * @return A modified copy of the {@code this} object
     */
    public final ImmutableFilterDistributiveRule.Config withDescription(@Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String value) {
      if (Objects.equals(this.description, value)) return this;
      return new ImmutableFilterDistributiveRule.Config(value, this.operandSupplier, this.relBuilderFactory);
    }

    /**
     * Copy the current immutable object by setting a value for the {@link FilterDistributiveRule.Config#operandSupplier() operandSupplier} attribute.
     * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
     * @param value A new value for operandSupplier
     * @return A modified copy of the {@code this} object
     */
    public final ImmutableFilterDistributiveRule.Config withOperandSupplier(RelRule.OperandTransform value) {
      if (this.operandSupplier == value) return this;
      RelRule.OperandTransform newValue = Objects.requireNonNull(value, "operandSupplier");
      return new ImmutableFilterDistributiveRule.Config(this.description, newValue, this.relBuilderFactory);
    }

    /**
     * Copy the current immutable object by setting a value for the {@link FilterDistributiveRule.Config#relBuilderFactory() relBuilderFactory} attribute.
     * A shallow reference equality check is used to prevent copying of the same value by returning {@code this}.
     * @param value A new value for relBuilderFactory
     * @return A modified copy of the {@code this} object
     */
    public final ImmutableFilterDistributiveRule.Config withRelBuilderFactory(RelBuilderFactory value) {
      if (this.relBuilderFactory == value) return this;
      RelBuilderFactory newValue = Objects.requireNonNull(value, "relBuilderFactory");
      return new ImmutableFilterDistributiveRule.Config(this.description, this.operandSupplier, newValue);
    }

    /**
     * This instance is equal to all instances of {@code Config} that have equal attribute values.
     * @return {@code true} if {@code this} is equal to {@code another} instance
     */
    @Override
    public boolean equals(@Nullable Object another) {
      if (this == another) return true;
      return another instanceof ImmutableFilterDistributiveRule.Config
          && equalTo(0, (ImmutableFilterDistributiveRule.Config) another);
    }

    private boolean equalTo(int synthetic, ImmutableFilterDistributiveRule.Config another) {
      return Objects.equals(description, another.description)
          && operandSupplier.equals(another.operandSupplier)
          && relBuilderFactory.equals(another.relBuilderFactory);
    }

    /**
     * Computes a hash code from attributes: {@code description}, {@code operandSupplier}, {@code relBuilderFactory}.
     * @return hashCode value
     */
    @Override
    public int hashCode() {
      @Var int h = 5381;
      h += (h << 5) + Objects.hashCode(description);
      h += (h << 5) + operandSupplier.hashCode();
      h += (h << 5) + relBuilderFactory.hashCode();
      return h;
    }

    /**
     * Prints the immutable value {@code Config} with attribute values.
     * @return A string representation of the value
     */
    @Override
    public String toString() {
      return MoreObjects.toStringHelper("Config")
          .omitNullValues()
          .add("description", description)
          .add("operandSupplier", operandSupplier)
          .add("relBuilderFactory", relBuilderFactory)
          .toString();
    }

    /**
     * Creates an immutable copy of a {@link FilterDistributiveRule.Config} value.
     * Uses accessors to get values to initialize the new immutable instance.
     * If an instance is already immutable, it is returned as is.
     * @param instance The instance to copy
     * @return A copied immutable Config instance
     */
    public static ImmutableFilterDistributiveRule.Config copyOf(FilterDistributiveRule.Config instance) {
      if (instance instanceof ImmutableFilterDistributiveRule.Config) {
        return (ImmutableFilterDistributiveRule.Config) instance;
      }
      return ImmutableFilterDistributiveRule.Config.builder()
          .from(instance)
          .build();
    }

    /**
     * Creates a builder for {@link ImmutableFilterDistributiveRule.Config Config}.
     * <pre>
     * ImmutableFilterDistributiveRule.Config.builder()
     *    .description(@org.checkerframework.checker.nullness.qual.Nullable String | null) // nullable {@link FilterDistributiveRule.Config#description() description}
     *    .operandSupplier(org.apache.calcite.plan.RelRule.OperandTransform) // optional {@link FilterDistributiveRule.Config#operandSupplier() operandSupplier}
     *    .relBuilderFactory(org.apache.calcite.tools.RelBuilderFactory) // optional {@link FilterDistributiveRule.Config#relBuilderFactory() relBuilderFactory}
     *    .build();
     * </pre>
     * @return A new Config builder
     */
    public static ImmutableFilterDistributiveRule.Config.Builder builder() {
      return new ImmutableFilterDistributiveRule.Config.Builder();
    }

    /**
     * Builds instances of type {@link ImmutableFilterDistributiveRule.Config Config}.
     * Initialize attributes and then invoke the {@link #build()} method to create an
     * immutable instance.
     * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
     * but instead used immediately to create instances.</em>
     */
    @Generated(from = "FilterDistributiveRule.Config", generator = "Immutables")
    @NotThreadSafe
    public static final class Builder {
      private @Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String description;
      private @Nullable RelRule.OperandTransform operandSupplier;
      private @Nullable RelBuilderFactory relBuilderFactory;

      private Builder() {
      }

      /**
       * Fill a builder with attribute values from the provided {@code org.apache.calcite.plan.RelRule.Config} instance.
       * @param instance The instance from which to copy values
       * @return {@code this} builder for use in a chained invocation
       */
      @CanIgnoreReturnValue 
      public final Builder from(RelRule.Config instance) {
        Objects.requireNonNull(instance, "instance");
        from((short) 0, (Object) instance);
        return this;
      }

      /**
       * Fill a builder with attribute values from the provided {@code edu.cmu.cs.db.calcite_app.app.rules.FilterDistributiveRule.Config} instance.
       * @param instance The instance from which to copy values
       * @return {@code this} builder for use in a chained invocation
       */
      @CanIgnoreReturnValue 
      public final Builder from(FilterDistributiveRule.Config instance) {
        Objects.requireNonNull(instance, "instance");
        from((short) 0, (Object) instance);
        return this;
      }

      private void from(short _unused, Object object) {
        @Var long bits = 0;
        if (object instanceof RelRule.Config) {
          RelRule.Config instance = (RelRule.Config) object;
          if ((bits & 0x2L) == 0) {
            this.relBuilderFactory(instance.relBuilderFactory());
            bits |= 0x2L;
          }
          if ((bits & 0x1L) == 0) {
            @Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String descriptionValue = instance.description();
            if (descriptionValue != null) {
              description(descriptionValue);
            }
            bits |= 0x1L;
          }
          if ((bits & 0x4L) == 0) {
            this.operandSupplier(instance.operandSupplier());
            bits |= 0x4L;
          }
        }
        if (object instanceof FilterDistributiveRule.Config) {
          FilterDistributiveRule.Config instance = (FilterDistributiveRule.Config) object;
          if ((bits & 0x2L) == 0) {
            this.relBuilderFactory(instance.relBuilderFactory());
            bits |= 0x2L;
          }
          if ((bits & 0x1L) == 0) {
            @Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String descriptionValue = instance.description();
            if (descriptionValue != null) {
              description(descriptionValue);
            }
            bits |= 0x1L;
          }
          if ((bits & 0x4L) == 0) {
            this.operandSupplier(instance.operandSupplier());
            bits |= 0x4L;
          }
        }
      }

      /**
       * Initializes the value for the {@link FilterDistributiveRule.Config#description() description} attribute.
       * @param description The value for description (can be {@code null})
       * @return {@code this} builder for use in a chained invocation
       */
      @CanIgnoreReturnValue 
      public final Builder description(@Nullable java.lang.@org.checkerframework.checker.nullness.qual.Nullable String description) {
        this.description = description;
        return this;
      }

      /**
       * Initializes the value for the {@link FilterDistributiveRule.Config#operandSupplier() operandSupplier} attribute.
       * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link FilterDistributiveRule.Config#operandSupplier() operandSupplier}.</em>
       * @param operandSupplier The value for operandSupplier 
       * @return {@code this} builder for use in a chained invocation
       */
      @CanIgnoreReturnValue 
      public final Builder operandSupplier(RelRule.OperandTransform operandSupplier) {
        this.operandSupplier = Objects.requireNonNull(operandSupplier, "operandSupplier");
        return this;
      }

      /**
       * Initializes the value for the {@link FilterDistributiveRule.Config#relBuilderFactory() relBuilderFactory} attribute.
       * <p><em>If not set, this attribute will have a default value as returned by the initializer of {@link FilterDistributiveRule.Config#relBuilderFactory() relBuilderFactory}.</em>
       * @param relBuilderFactory The value for relBuilderFactory 
       * @return {@code this} builder for use in a chained invocation
       */
      @CanIgnoreReturnValue 
      public final Builder relBuilderFactory(RelBuilderFactory relBuilderFactory) {
        this.relBuilderFactory = Objects.requireNonNull(relBuilderFactory, "relBuilderFactory");
        return this;
      }

      /**
       * Builds a new {@link ImmutableFilterDistributiveRule.Config Config}.
       * @return An immutable instance of Config
       * @throws java.lang.IllegalStateException if any required attributes are missing
       */
      public ImmutableFilterDistributiveRule.Config build() {
        return new ImmutableFilterDistributiveRule.Config(this);
      }
    }
  }
}
